package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.model.EcoUser;
import com.fyxridd.lib.core.api.model.InfoUser;
import com.fyxridd.lib.core.api.model.User;
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
        session.beginTransaction();
        @SuppressWarnings("unchecked")
        User user = (User) session.createQuery("from User u where u.lowerName=:lowerName")
                .setParameter("lowerName", name.toLowerCase()).uniqueResult();
        session.getTransaction().commit();
        session.close();
        return user;
    }

	/**
	 * 添加User
	 */
	public static void addOrUpdateUser(User user) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.saveOrUpdate(user);
		session.getTransaction().commit();
		session.close();
	}

    /**
     * 获取所有的EcoUser
     */
    public static List<EcoUser> getAllEcoUsers() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        @SuppressWarnings("unchecked")
        List<EcoUser> list = session.createQuery("from EcoUser").list();
        session.getTransaction().commit();
        session.close();
        return list;
    }

    /**
     * 添加或更新EcoUser
     */
    public static void addOrUpdateEcoUser(EcoUser user) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.saveOrUpdate(user);
        session.getTransaction().commit();
        session.close();
    }

    /**
     * 一次性保存多个玩家
     */
    public static void addOrUpdateEcoUsers(HashMap<String, EcoUser> ecoHash, Set<String> set) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        for (String name:set) {
            EcoUser eu = ecoHash.get(name);
            if (eu != null) session.saveOrUpdate(eu);
        }
        session.getTransaction().commit();
        session.close();
    }

    /**
     * @return 不存在返回null
     */
    public static InfoUser getInfo(String name, String flag) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        @SuppressWarnings("unchecked")
        InfoUser info = (InfoUser) session.createQuery("from InfoUser info where info.name=:name and info.flag=:flag")
                .setParameter("name", name).setParameter("flag", flag).uniqueResult();
        session.getTransaction().commit();
        session.close();
        return info;
    }

    /**
     * @return 可为空列表不为null
     */
    public static List<InfoUser> getInfos(String name) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        @SuppressWarnings("unchecked")
        List<InfoUser> infos = session.createQuery("from InfoUser info where info.name=:name")
                .setParameter("name", name).list();
        session.getTransaction().commit();
        session.close();
        if (infos == null) infos = new ArrayList<InfoUser>();
        return infos;
    }

    public static void updateInfos(Collection<InfoUser> update) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        for (InfoUser info:update) {
            if (info.getData() != null) session.saveOrUpdate(info);
            else session.delete(info);
        }
        session.getTransaction().commit();
        session.close();
    }
}
