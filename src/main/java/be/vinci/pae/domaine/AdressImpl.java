package be.vinci.pae.domaine;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdressImpl implements Adress {

  private int id;
  private String street;
  private int buildingNumber;
  private int unitNumber;
  private int postCode;
  private String commune;
  private String country;

  public int getID() {
    return id;
  }

  public void setID(int id) {
    this.id = id;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public int getBuildingNumber() {
    return buildingNumber;
  }

  public void setBuildingNumber(int buildingNumber) {
    this.buildingNumber = buildingNumber;
  }

  public int getUnitNumber() {
    return unitNumber;
  }

  public void setUnitNumber(int unitNumber) {
    this.unitNumber = unitNumber;
  }

  public int getPostCode() {
    return postCode;
  }

  public void setPostCode(int postCode) {
    this.postCode = postCode;
  }

  public String getCommune() {
    return commune;
  }

  public void setCommune(String commune) {
    this.commune = commune;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  @Override
  public String toString() {
    // TODO generate with source
    return "AdressImpl [id=" + id + ", Street=" + street + ", buildingNumber=" + buildingNumber
        + ", unitNumber=" + unitNumber + ", postCode=" + postCode + ", commune=" + commune
        + ", country=" + country + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AdressImpl other = (AdressImpl) obj;
    if (id != other.id) {
      return false;
    }
    return true;
  }

}