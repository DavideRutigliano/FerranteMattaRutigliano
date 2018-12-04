package com.github.ferrantemattarutigliano.software.client.model;

public class ThirdPartyDTO extends UserDTO{
    private String vat;
    private String email;
    private String organizationName;

    public ThirdPartyDTO(String username, String password) {
        super(username, password);
    }

    public ThirdPartyDTO(String username, String password, String vat, String email, String organizationName) {
        super(username, password);
        this.vat = vat;
        this.email = email;
        this.organizationName = organizationName;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}
