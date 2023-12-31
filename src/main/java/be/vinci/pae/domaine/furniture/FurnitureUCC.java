package be.vinci.pae.domaine.furniture;

import java.util.List;

public interface FurnitureUCC {

  FurnitureDTO add(FurnitureDTO furniture);

  FurnitureDTO findById(int id);

  List<FurnitureDTO> getAll();

  FurnitureDTO update(FurnitureDTO furniture);

  List<FurnitureDTO> getMyFurniture(int userID);

  Object[] getAllInfosForUpdate(int id);

  Object[] getAllInfosForAdd();

  Object[] getClientFurnitures(int id);

  List<FurnitureDTO> searchFurniture(boolean isBoss, String searchBar, int typeID, int minPrice,
      int maxPrice);
}
