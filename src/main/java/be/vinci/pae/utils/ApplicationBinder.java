package be.vinci.pae.utils;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import be.vinci.pae.domaine.DomaineFactory;
import be.vinci.pae.domaine.DomaineFactoryImpl;
import be.vinci.pae.domaine.UserUCC;
import be.vinci.pae.domaine.UserUCCImpl;
import be.vinci.pae.services.DalBackendServices;
import be.vinci.pae.services.DalServicesImpl;
import be.vinci.pae.services.UserDAO;
import be.vinci.pae.services.UserDAOImpl;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApplicationBinder extends AbstractBinder {

  @Override
  protected void configure() {
    bind(DomaineFactoryImpl.class).to(DomaineFactory.class).in(Singleton.class);
    bind(UserDAOImpl.class).to(UserDAO.class).in(Singleton.class);
    bind(UserUCCImpl.class).to(UserUCC.class).in(Singleton.class);
    bind(DalServicesImpl.class).to(DalBackendServices.class).in(Singleton.class);
  }

}
