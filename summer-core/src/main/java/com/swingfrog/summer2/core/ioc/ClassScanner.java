package com.swingfrog.summer2.core.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author: toke
 */
public class ClassScanner {

    private static final Logger log = LoggerFactory.getLogger(ClassScanner.class);

    public static Set<Class<?>> findClasses() {
        return findClasses("");
    }

    public static Set<Class<?>> findClasses(String pack) {
        return findClasses(pack, true);
    }

    public static Set<Class<?>> findClasses(String pack, boolean recursive) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        try {
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.charAt(0) == '/') {
                                name = name.substring(1);
                            }
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                if (idx != -1) {
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                if ((idx != -1) || recursive) {
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            log.error(e.getMessage(), e);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return classes;
    }

    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, boolean recursive, Set<Class<?>> classes) {
        if (packageName.length() > 0) {
            packageName = packageName + ".";
        }
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        assert dirFiles != null;
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + file.getName(),
                        file.getAbsolutePath(), recursive, classes);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + className));
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
