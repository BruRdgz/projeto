package com.bibliotecacultura.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Singleton wrapper around the JPA EntityManagerFactory.
 * Call JpaUtil.getEntityManager() to get a fresh EM; always close it after use.
 */
public final class JpaUtil {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("bibliotecaCulturaPU");

    private JpaUtil() {}

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /** Call once on application shutdown. */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
