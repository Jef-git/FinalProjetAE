package be.vinci.pae.domaine.delivery;

import java.sql.Timestamp;

public interface DeliveryDTO {

  int getId();

  void setId(int id);

  Timestamp getEstimatedDeliveryDate();

  void setEstimatedDeliveryDate(Timestamp estimatedDeliveryDate);

  boolean isDelivered();

  void setDelivered(boolean isDelivered);

  int getCustomer();

  void setCustomer(int customer);
}
