package be.vinci.pae.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import be.vinci.pae.api.utils.FatalException;
import be.vinci.pae.domaine.DomaineFactory;
import be.vinci.pae.domaine.address.AddressDTO;
import be.vinci.pae.domaine.user.UserDTO;
import jakarta.inject.Inject;

public class UserDAOImpl implements UserDAO {

  @Inject
  private DomaineFactory domaineFactory;

  @Inject
  private DalBackendServices dalBackendServices;



  @Override
  public UserDTO findByUserName(String username) {
    PreparedStatement ps = this.dalBackendServices.getPreparedStatement(
        "SELECT user_id, username, first_name, last_name, address, email, is_boss,"
            + " is_antique_dealer, is_confirmed, registration_date, password"
            + " FROM projet.users WHERE username = ?");
    UserDTO user = domaineFactory.getUserDTO();
    try {
      ps.setString(1, username);
      user = fullFillUserFromResulSet(user, ps);
    } catch (SQLException e) {
      e.printStackTrace();
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException(e.getMessage(), e);
    }
    if (user.getUserName() == null) {
      return null;
    }
    return user;
  }

  @Override
  public UserDTO findById(int id) {
    PreparedStatement ps = this.dalBackendServices.getPreparedStatement(
        "SELECT user_id, username, first_name, last_name, address, email, is_boss,"
            + " is_antique_dealer, is_confirmed, registration_date, password "
            + "FROM projet.users WHERE user_id = ?");
    UserDTO user = domaineFactory.getUserDTO();
    try {
      ps.setInt(1, id);
      user = fullFillUserFromResulSet(user, ps);
    } catch (SQLException e) {
      e.printStackTrace();
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException(e.getMessage(), e);
    }
    return user;
  }

  @Override
  public UserDTO registerUser(UserDTO user) {
    if (findByUserName(user.getUserName()) != null) {
      return null;
    }
    if (findByEmail(user.getEmail()) != null) {
      return null;
    }
    PreparedStatement ps = this.dalBackendServices.getPreparedStatement(
        "INSERT INTO projet.users VALUES(DEFAULT,?,?,?,?,?,?,DEFAULT,DEFAULT,DEFAULT,?)");
    try {
      ps.setString(1, user.getLastName());
      ps.setString(2, user.getFirstName());
      ps.setString(3, user.getUserName());
      ps.setString(4, user.getPassword());
      ps.setInt(5, user.getAdressID());
      ps.setString(6, user.getEmail());
      ps.setTimestamp(7, user.getRegistrationDate());
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException(e.getMessage(), e);
    }
    return findByUserName(user.getUserName());
  }

  @Override
  public int registerAddress(AddressDTO addressDTO) {
    if (getAddressByInfo(addressDTO.getStreet(), addressDTO.getBuildingNumber(),
        addressDTO.getCommune(), addressDTO.getCountry()) > 0) {
      return -1;
    }
    PreparedStatement ps = this.dalBackendServices
        .getPreparedStatement("INSERT INTO projet.addresses VALUES(DEFAULT,?,?,?,?,?,?)");
    try {
      ps.setString(1, addressDTO.getStreet());
      ps.setString(2, addressDTO.getBuildingNumber());
      ps.setString(3, addressDTO.getPostCode());
      ps.setString(4, addressDTO.getCommune());
      ps.setString(5, addressDTO.getCountry());
      ps.setString(6, addressDTO.getUnitNumber());
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException(e.getMessage(), e);
    }
    int i = getAddressByInfo(addressDTO.getStreet(), addressDTO.getBuildingNumber(),
        addressDTO.getCommune(), addressDTO.getCountry());
    return i;
  }

  @Override
  public int getAddressByInfo(String street, String buildingNumber, String commune,
      String country) {
    PreparedStatement ps = this.dalBackendServices
        .getPreparedStatement("SELECT address_id FROM projet.addresses WHERE street=? "
            + "AND building_number=? AND country=? AND commune=?");
    int adresse = 0;
    try {
      ps.setString(1, street);
      ps.setString(2, buildingNumber);
      ps.setString(3, country);
      ps.setString(4, commune);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          adresse = rs.getInt(1);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException(e.getMessage(), e);
    }
    if (adresse <= 0) {
      return -1;
    }
    return adresse;
  }

  @Override
  public List<UserDTO> getAll() {
    PreparedStatement ps =
        this.dalBackendServices.getPreparedStatement("SELECT user_id , last_name , "
            + "first_name,username ,password , address , email , is_boss ,"
            + " is_antique_dealer , is_confirmed , " + " registration_date FROM projet.users"
            + " ORDER BY user_id");


    List<UserDTO> list = new ArrayList<UserDTO>();

    try (ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        UserDTO user = domaineFactory.getUserDTO();
        fullFillListUsers(rs, user);
        list.add(user);
      }
    } catch (SQLException e) {
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException("error fullFillUsers", e);
    }

    return list;
  }

  @Override
  public List<UserDTO> getAllSearchedUser(String search) {
    PreparedStatement ps = this.dalBackendServices.getPreparedStatement(
        "SELECT * FROM projet.users u, projet.addresses a " + "WHERE u.address = a.address_id AND"
            + " (lower(u.last_name) LIKE lower(?) OR lower(a.commune) LIKE lower(?)"
            + " OR lower(a.postcode) LIKE lower(?))");

    List<UserDTO> list = new ArrayList<UserDTO>();
    try {
      search = '%' + search + '%';
      ps.setString(1, search);
      ps.setString(2, search);
      ps.setString(3, search);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        UserDTO user = domaineFactory.getUserDTO();
        fullFillListUsers(rs, user);
        list.add(user);
      }
    } catch (SQLException e) {
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException("error fullFillUsers", e);
    }
    return list;
  }

  @Override
  public List<UserDTO> getAllConfirmed(boolean isConfirmed) {
    PreparedStatement ps =
        this.dalBackendServices.getPreparedStatement("SELECT user_id , last_name , "
            + "first_name,username ,password , address , email , is_boss ,"
            + " is_antique_dealer , is_confirmed," + " registration_date FROM projet.users "
            + "WHERE is_confirmed =?");


    List<UserDTO> list = new ArrayList<UserDTO>();

    try {
      ps.setBoolean(1, isConfirmed);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        UserDTO user = domaineFactory.getUserDTO();
        fullFillListUsers(rs, user);
        list.add(user);
      }
    } catch (SQLException e) {
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException("error fullFillUsers", e);
    }

    return list;
  }

  @Override
  public void updateConfirmed(UserDTO user) {
    PreparedStatement ps = this.dalBackendServices
        .getPreparedStatement("UPDATE projet.users SET is_confirmed = ? , is_antique_dealer = ?"
            + ", is_boss=? WHERE user_id = ?");


    try {
      ps.setBoolean(1, user.isConfirmed());
      ps.setBoolean(2, user.isAntiqueDealer());
      ps.setBoolean(3, user.isBoss());
      ps.setInt(4, user.getID());
      ps.executeUpdate();
    } catch (SQLException e) {
      ((DalServices) dalBackendServices).rollbackTransaction();
      e.printStackTrace();
      throw new FatalException(e.getMessage(), e);
    }
  }

  @Override
  public AddressDTO getAddressById(int id) {
    PreparedStatement ps =
        this.dalBackendServices.getPreparedStatement("SELECT address_id ,street ,building_number,"
            + "postcode,commune , country,unit_number FROM projet.addresses WHERE address_id=?");
    AddressDTO address = domaineFactory.getAdressDTO();
    try {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          address = fullFillAddressFromResultSet(rs);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException(e.getMessage(), e);
    }

    return address;
  }

  @Override
  public AddressDTO getVisitAddress(int addressId, int userId) {
    PreparedStatement ps = this.dalBackendServices
        .getPreparedStatement("SELECT a.address_id, a.street, a.building_number,"
            + " a.postcode, a.commune, a.country, a.unit_number" + " FROM projet.addresses a"
            + " JOIN projet.visits v ON a.address_id = v.address"
            + " WHERE a.address_id = ? AND v.users = ?");
    AddressDTO address = domaineFactory.getAdressDTO();
    try {
      ps.setInt(1, addressId);
      ps.setInt(2, userId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          address = fullFillAddressFromResultSet(rs);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException(e.getMessage(), e);
    }

    return address;
  }



  // ******************** Private's Methods ********************

  /**
   * Fully fill the user with the ResultSet from de db. Or throws SQLException.
   * 
   * @param user empty, to be filled.
   * @param ps the PreparedStatement already Set.
   * @return the user filled.
   * @throws SQLException if problems.
   */
  private UserDTO fullFillUserFromResulSet(UserDTO user, PreparedStatement ps) throws SQLException {
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
    return user;
  }

  private UserDTO findByEmail(String email) {
    PreparedStatement ps = this.dalBackendServices.getPreparedStatement(
        "SELECT user_id, username, first_name, last_name, address, email, is_boss,"
            + " is_antique_dealer, is_confirmed, registration_date, password"
            + " FROM projet.users WHERE email = ?");
    UserDTO user = domaineFactory.getUserDTO();
    try {
      ps.setString(1, email);
      user = fullFillUserFromResulSet(user, ps);
    } catch (SQLException e) {
      e.printStackTrace();
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException(e.getMessage(), e);
    }
    if (user.getEmail() == null) {
      return null;
    }
    return user;
  }

  private UserDTO fullFillListUsers(ResultSet rs, UserDTO user) {
    try {
      user.setID(rs.getInt(1));
      user.setLastName(rs.getString(2));
      user.setFirstName(rs.getString(3));
      user.setUserName(rs.getString(4));
      user.setPassword(rs.getString(5));
      user.setAdressID(rs.getInt(6));
      user.setEmail(rs.getString(7));
      user.setBoss(rs.getBoolean(8));
      user.setAntiqueDealer(rs.getBoolean(9));
      user.setConfirmed(rs.getBoolean(10));
      user.setRegistrationDate(rs.getTimestamp(11));

    } catch (SQLException e) {
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException("error fullFillUsers", e);
    }
    return user;
  }

  private AddressDTO fullFillAddress(int id, String street, String buildingNumber, String postCode,
      String commune, String country, String unitNumber) {
    AddressDTO address = domaineFactory.getAdressDTO();

    address.setID(id);
    address.setStreet(street);
    address.setBuildingNumber(buildingNumber);
    address.setPostCode(postCode);
    address.setCommune(commune);
    address.setCountry(country);
    address.setUnitNumber(unitNumber);

    return address;
  }

  private AddressDTO fullFillAddressFromResultSet(ResultSet rs) {
    try {
      return fullFillAddress(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
          rs.getString(5), rs.getString(6), rs.getString(7));
    } catch (SQLException e) {
      ((DalServices) dalBackendServices).rollbackTransaction();
      throw new FatalException("error fullFillAddress", e);
    }
  }
}
