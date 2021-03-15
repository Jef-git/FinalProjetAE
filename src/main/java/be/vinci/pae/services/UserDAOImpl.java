package be.vinci.pae.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import be.vinci.pae.domaine.Adress;
import be.vinci.pae.domaine.AdressFactory;
import be.vinci.pae.domaine.UserDTO;
import be.vinci.pae.domaine.UserFactory;
import jakarta.inject.Inject;

public class UserDAOImpl implements UserDAO {

  @Inject
  private UserFactory userFactory;

  @Inject
  private AdressFactory adressFactory;

  @Inject
  private DalServices dalServices;

  @Override
  public UserDTO findByUserName(String username) {
    PreparedStatement ps = this.dalServices.getPreparedStatement(
        "SELECT user_id, username, first_name, last_name, address, email, is_boss,\r\n"
            + "            is_antique_dealer, is_confirmed, registration_date, password \r\n"
            + "            FROM projet.users WHERE username = ?");
    UserDTO user = userFactory.getUserDTO();
    try {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          user.setID(rs.getInt(1));
          user.setUserName(rs.getString(2));
          user.setFirstName(rs.getString(3));
          user.setLastName(rs.getString(4));
          user.setAdressID(rs.getInt(5));
          user.setEmail(rs.getString(6));
          user.setBoss(rs.getBoolean(7));
          user.setAntiqueDealer(rs.getBoolean(8));
          user.setConfirmed(rs.getBoolean(9));
          user.setRegistrationDate(rs.getTimestamp(10));
          user.setPassword(rs.getString(11));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
    return user;
  }

  @Override
  public UserDTO findById(int id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UserDTO registerUser(UserDTO user) {
    PreparedStatement ps = this.dalServices.getPreparedStatement(
        "INSERT INTO projet.users " + "VALUES(DEFAULT,?,?,?,?,?,?,DEFAULT,DEFAULT,DEFAULT,?)");
    try {
      ps.setString(1, user.getLastName());
      ps.setString(2, user.getFirstName());
      ps.setString(4, user.getUserName());
      ps.setString(3, user.getPassword());
      ps.setInt(5, user.getAdressID());
      ps.setString(6, user.getEmail());
      ps.setTimestamp(7, user.getRegistrationDate());
      ps.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
    return findByUserName(user.getUserName());
  }

  @Override
  public Adress registerAdress(Adress adress) {
    PreparedStatement ps = this.dalServices
        .getPreparedStatement("INSERT INTO projet.addresses VALUES(DEFAULT,?,?,?,?,?,?)");
    try {
      ps.setString(1, adress.getStreet());
      ps.setString(2, adress.getBuildingNumber());
      ps.setString(3, adress.getPostCode());
      ps.setString(4, adress.getCommune());
      ps.setString(5, adress.getCountry());
      ps.setString(6, adress.getUnitNumber());
      ps.executeQuery();
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
    getAdressById(adress.getID());
    return adress;
  }

  @Override
  public Adress getAdressById(int adress_id) {
    PreparedStatement ps = this.dalServices.getPreparedStatement("SELECT address_id,street,"
        + "building_number,postcode,commune,country,unit_number FROM projet.addresses WHERE address_id=?");
    Adress adresse = adressFactory.getAdress();
    try {
      ps.setInt(1, adress_id);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          adresse.setID(rs.getInt(1));
          adresse.setStreet(rs.getString(2));
          adresse.setBuildingNumber(rs.getString(3));
          adresse.setPostCode(rs.getString(4));
          adresse.setCommune(rs.getString(5));
          adresse.setCountry(rs.getString(6));
          adresse.setUnitNumber(rs.getString(7));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
    return adresse;
  }

}
