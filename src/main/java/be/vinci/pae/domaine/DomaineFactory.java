package be.vinci.pae.domaine;

import be.vinci.pae.domaine.address.AddressDTO;
import be.vinci.pae.domaine.furniture.FurnitureDTO;
import be.vinci.pae.domaine.option.OptionDTO;
import be.vinci.pae.domaine.photo.PhotoDTO;
import be.vinci.pae.domaine.photo.PhotoFurnitureDTO;
import be.vinci.pae.domaine.type.TypeDTO;
import be.vinci.pae.domaine.user.UserDTO;

public interface DomaineFactory {
  UserDTO getUserDTO();

  FurnitureDTO getFurnitureDTO();

  AddressDTO getAdressDTO();

  OptionDTO getOptionDTO();

  TypeDTO getTypeDTO();

  PhotoDTO getPhotoDTO();

  PhotoFurnitureDTO getPhotoFurnitureDTO();
}
