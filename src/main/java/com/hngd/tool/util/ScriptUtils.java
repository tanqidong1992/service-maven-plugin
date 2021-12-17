package com.hngd.tool.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class ScriptUtils {

    public static void addExecutePermission(File script) throws IOException {
        PosixFileAttributes attributes=Files.readAttributes(script.toPath(), PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        Set<PosixFilePermission> permissions=attributes.permissions();
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(script.toPath(),permissions);
    }
}
