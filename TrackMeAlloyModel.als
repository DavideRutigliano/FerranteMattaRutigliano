module trackMe
open util/boolean

sig Username, Password{}

abstract sig User{
	username: Username,
	password: Password
}

sig Name, OrganizationName, Ssn, Vat{}

sig Individual extends User{
	name: Name,
	incomingRequests: set IndividualRequest,
	ssn: Ssn,
	enableSos: Bool
}

abstract sig Request{
	sender: ThirdParty,
	receiver: some Individual,
	accepted : Bool 
}

sig IndividualRequest extends Request{
	ssn: Ssn,
}

sig GroupRequest extends Request{}

sig ThirdParty extends User{
	organization: OrganizationName,
	sentRequests: set Request,
	subscribedUsers: set Individual,
	vat: Vat
}

sig Ambulance{
	available: Bool
}

sig AutomatedSos{
	provider: ThirdParty,
	customers: set Individual,
	ambulances: some Ambulance
}

sig Track, Duration, Date, Time{}

sig Run{
	organizer: Organizer,
	track: Track,
	duration: Duration,
	date: Date,
	time: Time,
}

sig Athlete extends Individual{
	enrolledRuns: set Run
}

sig Spectator extends Individual{
	watchedRuns : set Run
}

sig Organizer extends Individual{
	organizedRuns : set Run
}

--														FACTS 												--

fact dataAtomicity{
	all u:Username | some user:User | user.username = u
	all p:Password | some user:User | user.password = p
	all n:Name | some i:Individual | i.name = n
	all n:OrganizationName | some t:ThirdParty | t.organization = n
	all s:Ssn | some i:Individual | i.ssn = s
	all v:Vat | some t:ThirdParty | t.vat= v
	all t:Track | some r:Run | r.track = t
	all d:Duration | some r:Run | r.duration = d	
	all d:Date | some r:Run | r.date = d	
	all t:Time | some r:Run | r.time = t		
}

fact dataUniqueness{
	no disj u1,u2: User | u1.username = u2.username 						//username
	no disj i1,i2: Individual | i1.ssn = i2.ssn											//SSN
	no disj p1,p2: ThirdParty | p1.vat = p2.vat										//VAT
	no disj p1,p2: ThirdParty | p1.organization = p2.organization		//organization name
}

--													DATA4HELP 												--

fact individualRequestsAreUnary{
	all r:IndividualRequest | #r.receiver = 1
}

fact sentRequestAreRecorded{
	all r: Request, t: ThirdParty | r in t.sentRequests iff r.sender = t
}

fact receivedRequestAreRecorded {
	all r: IndividualRequest, i: Individual | r in i.incomingRequests iff r.receiver = i
}

fact requestSsnPointAtCorrectReceiver{
	all r: IndividualRequest, i: Individual | r.ssn = i.ssn iff i.ssn = r.receiver.ssn
}

-- a groupRequest will be accepted only if it is anonymous
fact grantAnonimity{
	all r: GroupRequest | isTrue[r.accepted] iff hasAnonimity[r]
}

-- to subscribe to an individual's new data, a third party must have sent a request that has been accepted
fact subscriptionMustBeAccepted{
	all t:ThirdParty, i:Individual, r:IndividualRequest | i in t.subscribedUsers => (requestBetween[r, t, i] and isTrue[r.accepted])
}

--												AUTOMATED-SOS 											--

-- a Third party can provide only one automated-sos service
fact uniqueAutomatedSosService {
	no disj a1, a2: AutomatedSos | all t: ThirdParty | enabledService[a1, t] and enabledService[a2, t]
}

-- an Individual can be an Automated-SOS customer only if he has activated the service
fact enabledSosMeansCustomer{
	all a: AutomatedSos, i: Individual | i in a.customers => isTrue[i.enableSos] 
}

-- an Individual can be subscribed to only one Automated-SOS provider
fact justOneAutomatedSosSub{
	no disj a1,a2: AutomatedSos | all i: Individual | isCustomer[i, a1] and isCustomer[i, a2]
}

-- automatedSos services only list available ambulances
fact onlyAvailableAmbulances{
	all a: Ambulance, s: AutomatedSos | a in s.ambulances => isTrue[a.available]
}


--													TRACK4RUN 												--

fact organizedRunsAreRecorded{
	all r:Run, o:Organizer | r.organizer = o iff r in o.organizedRuns
}

-- there can't exist two runs that have the same track in the same date
fact noDuplicatedRun{
	no disj r1,r2: Run | isSameRun[r1,r2]
}

-- an athlete can't enroll 2 runs that happens in the same date/time
fact noMultipleEnrollement{
	all disj r1,r2: Run | all a:Athlete | (isEnrolled[r1,a] and isEnrolled[r2,a]) => not isSameDate[r1,r2]
}

-- a spectator can't enroll 2 runs that happens in the same date/time
fact noMultipleWatch{
	all disj r1,r2:Run | all s:Spectator | (isEnrolled[r1,s] and isEnrolled[r2,s]) => not isSameDate[r1,r2]
}

-- an athlete can't watch the runs where he is also enrolled
fact athletesCantWatch{
	no s: Spectator, a: Athlete | isSameIndividual[s,a] and #a.enrolledRuns > 0 and hasCommonRuns[s,a]
}

-- if a run exists it must be organized by some organizer
fact runMustBeOrganized{
	all r:Run | some o:Organizer | hasOrganized[r,o]
}


--														PREDICATES 											--

--														DATA4HELP 												--

pred isSameRequest[r1,r2:Request]{
	r1.receiver = r2.receiver and r1.sender = r2.sender	
}

pred requestBetween[r:Request, t:ThirdParty, i:Individual]{
	r.sender = t and r.receiver = i 	
}

pred isSubscribedToData[t:ThirdParty, i:Individual]{
	i in t.subscribedUsers
}

pred hasAnonimity[r: GroupRequest]{
	#r.receiver > 1000	
}

--													AUTOMATED-SOS 											--

pred enabledService[a: AutomatedSos, p: ThirdParty]{
	a.provider = p
}

pred isCustomer[i:Individual, a:AutomatedSos]{
	i in a.customers		
}

--														TRACK4RUN 												--

pred isSameIndividual[s:Spectator, a:Athlete]{
	s.ssn = a.ssn
}

pred hasCommonRuns[s:Spectator, a:Athlete]{
	a.enrolledRuns & s.watchedRuns != none
}

pred isEnrolled[r: Run, a:Athlete]{
	r in a.enrolledRuns
}

pred isEnrolled[r: Run, s:Spectator]{
	r in s.watchedRuns
}

pred hasOrganized[r:Run, o:Organizer]{
	r in o.organizedRuns
}

pred isSameDate[r1, r2 : Run]{
	r1.date = r2.date and r1.time = r2.time
}

pred isSameRun [r1, r2 : Run]{
	isSameDate[r1,r2] and r1.track = r2.track	
}

pred runAutomatedSos(a: AutomatedSos, p: ThirdParty){
	#Ambulance = 2
	#Run = 0
	a.provider = p
}

------------------------------------------------------------------------------------------------

pred disableData4Help{
	#Request = 0
	#ThirdParty.subscribedUsers = 0
}

pred disableAutomatedSos{
	#AutomatedSos = 0
	#Ambulance = 0
}

pred disableTrack4Run{
	#Athlete = 0
	#Spectator = 0
	#Organizer = 0
	#Run = 0
}

pred data4Help{
	disableTrack4Run
	disableAutomatedSos
	some IndividualRequest
	some GroupRequest
}

pred automatedSos{
	disableData4Help
	disableTrack4Run
	some Ambulance
	some AutomatedSos	
}

pred track4Run{
	disableData4Help
	disableAutomatedSos
	some Organizer
	some Athlete
	some Spectator
	some Run
}

pred showAll{}

run data4Help for 3
//run automatedSos for 3
//run track4Run for 3
//run showAll for 3