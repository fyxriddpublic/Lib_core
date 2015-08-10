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
    private static final List<File> hbms = new ArrayList<File>();

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
     * @return 不存在返回null
     */
    public static User getUser(String name) {
        Session session = sessionFactory.openSession();
        User user;
        try {
            session.beginTransaction();
            user = (User) session.createQuery("from User u where u.lowerName=:lowerName")
                    .setParameter("lowerName", name.toLowerCase()).uniqueResult();
            session.getTransaction().commit();
        } finally {
            session.close();
        }
        return user;
    }

	/**
	 * 添加User
	 */
	public static void addOrUpdateUser(User user) {
		Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.saveOrUpdate(user);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
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

    /**
     * 获取所有的EcoUser
     */
    public static List<EcoUser> getAllEcoUsers() {
        Session session = sessionFactory.openSession();
        List<EcoUser> list;
        try {
            session.beginTransaction();
            list = session.createQuery("from EcoUser").list();
            session.getTransaction().commit();
        } finally {
            session.close();
        }
        return list;
    }

    /**
     * 添加或更新EcoUser
     */
    public static void addOrUpdateEcoUser(EcoUser user) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.saveOrUpdate(user);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    /**
     * 一次性保存多个玩家
     */
    public static void addOrUpdateEcoUsers(HashMap<String, EcoUser> ecoHash, Set<String> set) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            for (String name:set) {
                EcoUser eu = ecoHash.get(name);
                if (eu != null) session.saveOrUpdate(eu);
            }
            session.getTransaction().commit();
        } finally {
            session.close();
        }
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

    /**
     * @return 可为空列表不为null
     */
    public static List<InfoUser> getInfos(String name) {
        Session session = sessionFactory.openSession();
        List<InfoUser> infos;
        try {
            session.beginTransaction();
            infos = session.createQuery("from InfoUser info where info.name=:name")
                    .setParameter("name", name).list();
            session.getTransaction().commit();
        } finally {
            session.close();
        }
        if (infos == null) infos = new ArrayList<InfoUser>();
        return infos;
    }

    public static void updateInfos(Collection<InfoUser> update) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            for (InfoUser info:update) {
                if (info.getData() != null) session.saveOrUpdate(info);
                else session.delete(info);
            }
            session.getTransaction().commit();
        } finally {
            session.close();
        }
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
