package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.util.*;

public class Dao {
    private static final List<File> hbms = new ArrayList<>();

	private static SessionFactory sessionFactory;
	
	public Dao() {
		Configuration config = new Configuration().configure(new File(CorePlugin.dataPath+File.separator+"hibernate.cfg.xml"));
        for (File hbm:hbms) config.addFile(hbm);
		StandardServiceRegistry service = new StandardServiceRegistryBuilder().applySettings(config.getProperties()).build();
		sessionFactory = config.buildSessionFactory(service);
	}

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @see com.fyxridd.lib.core.api.CorePlugin#registerHbm(java.io.File)
     */
    public static void registerHbm(File hbm) {
        hbms.add(hbm);
    }

	public static void close() {
		sessionFactory.close();
	}

    /**
     * @return 可为null
     */
    public static User getUser(String name) {
        Session session = sessionFactory.openSession();
        User user;
        try {
            session.beginTransaction();
            user = (User) session.createQuery("from User where lowerName=:lowerName")
                    .setParameter("lowerName", name.toLowerCase()).uniqueResult();
            session.getTransaction().commit();
        } finally {
            session.close();
        }
        return user;
    }

    public void saveOrUpdate(Object obj) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.saveOrUpdate(obj);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    public void saveOrUpdates(Collection c) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            for (Object o:c) session.saveOrUpdate(o);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    public void delete(Object obj) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.delete(obj);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    public void deletes(Collection c) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            for (Object o:c) session.delete(o);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    /**
     * 获取所有的EcoUser
     */
    public static List<EcoUser> getAllEcoUsers() {
        Session session = sessionFactory.openSession();
        List<EcoUser> list;
        try {
            session.beginTransaction();
            list = session.createQuery("from EcoUser where name!=''").list();
            session.getTransaction().commit();
        } finally {
            session.close();
        }
        return list;
    }

    /**
     * @return 不存在返回null
     */
    public static InfoUser getInfo(String name, String flag) {
        Session session = sessionFactory.openSession();
        InfoUser info;
        try {
            session.beginTransaction();
            info = (InfoUser) session.createQuery("from InfoUser info where info.name=:name and info.flag=:flag")
                    .setParameter("name", name).setParameter("flag", flag).uniqueResult();
            session.getTransaction().commit();
        } finally {
            session.close();
        }
        return info;
    }

    public List<PerGroup> getPerGroups() {
        Session session = sessionFactory.openSession();
        List<PerGroup> result;
        try {
            session.beginTransaction();
            result = session.createQuery("from PerGroup where name != ''").list();
            session.getTransaction().commit();
        } finally {
            session.close();
        }
        return result;
    }

    public PerUser getPerUser(String name) {
        Session session = sessionFactory.openSession();
        PerUser result;
        try {
            session.beginTransaction();
            result = (PerUser) session.createQuery("from PerUser where name=:name").setParameter("name", name).uniqueResult();
            session.getTransaction().commit();
        } finally {
            session.close();
        }
        return result;
    }
}
