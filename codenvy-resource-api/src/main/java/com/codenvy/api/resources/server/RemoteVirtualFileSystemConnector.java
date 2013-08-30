/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.api.resources.server;

import com.codenvy.api.resources.shared.AccessControlList;
import com.codenvy.api.resources.shared.Attribute;
import com.codenvy.api.resources.shared.AttributeProvider;
import com.codenvy.api.resources.shared.Attributes;
import com.codenvy.api.resources.shared.File;
import com.codenvy.api.resources.shared.Folder;
import com.codenvy.api.resources.shared.Project;
import com.codenvy.api.resources.shared.Resource;
import com.codenvy.api.resources.shared.ResourceAccessControlEntry;
import com.codenvy.api.vfs.dto.ItemDto;
import com.codenvy.api.vfs.server.exceptions.ConstraintException;
import com.codenvy.api.vfs.server.exceptions.InvalidArgumentException;
import com.codenvy.api.vfs.server.exceptions.ItemAlreadyExistException;
import com.codenvy.api.vfs.server.exceptions.ItemNotFoundException;
import com.codenvy.api.vfs.server.exceptions.LockException;
import com.codenvy.api.vfs.server.exceptions.NotSupportedException;
import com.codenvy.api.vfs.server.exceptions.PermissionDeniedException;
import com.codenvy.api.vfs.server.exceptions.VirtualFileSystemException;
import com.codenvy.api.vfs.server.util.DeleteOnCloseFileInputStream;
import com.codenvy.api.vfs.shared.AccessControlEntry;
import com.codenvy.api.vfs.shared.AccessControlEntryImpl;
import com.codenvy.api.vfs.shared.ExitCodes;
import com.codenvy.api.vfs.shared.Item;
import com.codenvy.api.vfs.shared.ItemList;
import com.codenvy.api.vfs.shared.ItemListImpl;
import com.codenvy.api.vfs.shared.Link;
import com.codenvy.api.vfs.shared.Lock;
import com.codenvy.api.vfs.shared.LockImpl;
import com.codenvy.api.vfs.shared.Property;
import com.codenvy.api.vfs.shared.PropertyImpl;
import com.codenvy.api.vfs.shared.VirtualFileSystemInfo;
import com.codenvy.api.vfs.shared.VirtualFileSystemInfoImpl;
import com.codenvy.commons.json.JsonHelper;
import com.codenvy.commons.json.JsonParseException;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.commons.lang.cache.Cache;
import com.codenvy.commons.lang.cache.SLRUCache;
import com.codenvy.core.api.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/** @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a> */
public class RemoteVirtualFileSystemConnector extends VirtualFileSystemConnectorImpl {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteVirtualFileSystemConnector.class);

    static {
        if (CookieHandler.getDefault() == null) {
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        }
    }

    private final Cache<String, Item>   cache;
    private final VirtualFileSystemInfo vfsInfo;
    private final Folder                root;

    public RemoteVirtualFileSystemConnector(String name, String url) {
        super(name);
        cache = new SLRUCache<>(64, 32);
        vfsInfo = get(url, VirtualFileSystemInfoImpl.class, 200);
        root = new Folder(this, null, vfsInfo.getRoot().getId(), vfsInfo.getRoot().getName());
    }

    @Override
    public Folder getRoot() {
        return root;
    }

    @Override
    public Resource getResource(Folder parent, String name) {
        final String path = parent.createPath(name).substring(1);
        final String url = createUrl(vfsInfo.getUrlTemplates().get(Link.REL_ITEM_BY_PATH), Pair.of("path", path));
        final Item item = get(url, ItemDto.class, 200);
        cache.put(item.getId(), item);
        return createResource(parent, item);
    }

    @Override
    public List<Resource> getChildResources(Folder parent) {
        final String url = getVfsItem(parent).getLinks().get(Link.REL_CHILDREN).getHref();
        final ItemList itemList = get(url, ItemListImpl.class, 200);
        final List<Item> items = itemList.getItems();
        if (items == null || items.isEmpty()) {
            return new ArrayList<>(0);
        }

        final List<Resource> children = new ArrayList<>(items.size());
        for (Item item : items) {
            children.add(createResource(parent, item));
        }
        return children;
    }

    @Override
    public File createFile(Folder parent, String name) {
        final String url = createUrl(vfsInfo.getUrlTemplates().get(Link.REL_CREATE_FILE), Pair.of("name", name),
                                     Pair.of("parentId", parent.getId()));
        final Item item = post(url, ItemDto.class, null, 200);
        cache.put(item.getId(), item);
        return new FileImpl(this, parent, item.getId(), item.getName());
    }

    @Override
    public Folder createFolder(Folder parent, String name) {
        final String url = createUrl(vfsInfo.getUrlTemplates().get(Link.REL_CREATE_FOLDER), Pair.of("name", name),
                                     Pair.of("parentId", parent.getId()));
        final Item item = post(url, ItemDto.class, null, 200);
        cache.put(item.getId(), item);
        return new Folder(this, parent, item.getId(), item.getName());
    }

    @Override
    public Project createProject(String name) {
        return createProject(null, name);
    }

    @Override
    public Project createProject(Project parent, String name) {
        Folder parentFolder = parent;
        if (parent == null) {
            parentFolder = root;
        }
        final String url = createUrl(vfsInfo.getUrlTemplates().get(Link.REL_CREATE_PROJECT), Pair.of("name", name),
                                     Pair.of("parentId", parentFolder.getId()));
        final Item item = post(url, ItemDto.class, null, 200);
        cache.put(item.getId(), item);
        return new Project(this, parentFolder, item.getId(), item.getName());
    }

    @Override
    public void delete(Resource resource) {
        if (resource.isFolder()) {
            // To simplify working with cache just clear it when remove folder.
            cache.clear();
        } else {
            cache.remove(resource.getId());
        }
        post(getVfsItem(resource).getLinks().get(Link.REL_DELETE).getHref(), null, null, 204);
    }

    @Override
    public String getContent(File file) {
        try {
            return IoUtil.readAndCloseQuietly(getContentStream(file));
        } catch (IOException e) {
            throw new VirtualFileSystemUnknownException(e);
        }
    }

    @Override
    public void updateContent(File file, String data, String contentType) {
        updateContentStream(file, new ByteArrayInputStream(data.getBytes()), contentType);
    }

    public InputStream getContentStream(File file) {
        final String url = getVfsItem(file).getLinks().get(Link.REL_CONTENT).getHref();
        HttpURLConnection conn = null;
        final int responseCode;
        try {
            conn = (HttpURLConnection)new URL(url).openConnection();
            authenticate(conn);
            responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw restoreRemoteException(responseCode, conn);
            }
            try (InputStream in = conn.getInputStream()) {
                return bufferStream(in, 65536);
            }
        } catch (IOException e) {
            throw new VirtualFileSystemUnknownException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public void updateContentStream(File file, InputStream data, String contentType) {
        final String url = getVfsItem(file).getLinks().get(Link.REL_CONTENT).getHref();
        cache.remove(file.getId());
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)new URL(url).openConnection();
            authenticate(conn);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("content-type", contentType);
            conn.setDoOutput(true);
            try (OutputStream output = conn.getOutputStream()) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = data.read(buf)) > 0) {
                    output.write(buf, 0, r);
                }
            }

            final int responseCode = conn.getResponseCode();
            if (responseCode != 204) {
                throw restoreRemoteException(responseCode, conn);
            }
        } catch (IOException e) {
            throw new VirtualFileSystemUnknownException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    public Attributes getAttributes(Resource resource) {
        return new AttributesImpl(this, resource);
    }

    @Override
    public void updateAttributes(Attributes attributes) {
        final List<Attribute<?>> all = attributes.getAll();
        if (all.isEmpty()) {
            return;
        }
        for (Iterator<Attribute<?>> i = all.iterator(); i.hasNext(); ) {
            Attribute<?> attribute = i.next();
            if (!(attribute.isUpdated() && attribute.isPersistent())) {
                i.remove();
            }
        }
        if (all.isEmpty()) {
            return;
        }

        final List<Property> props = new ArrayList<>();
        for (Attribute<?> update : all) {
            final String name = update.getName();
            final Object value = update.getValue();
            final AttributeProvider<?> attrProv = AttributesImpl.getAttributeProvider(name);
            props.add(new PropertyImpl(attrProv.getVfsPropertyName(), value == null ? null : String.valueOf(value)));
        }

        Item item = getVfsItem(attributes.getResource());
        cache.remove(item.getId());
        final String url = item.getLinks().get(Link.REL_SELF).getHref();
        item = post(url, ItemDto.class, props, 200);
        cache.put(item.getId(), item);
    }

    @Override
    public Resource rename(Resource resource, String newname, String contentType) {
        if (contentType == null) {
            // restore defaults ?
            if (resource.isFile()) {
                contentType = ((File)resource).getContentType();
            } else if (resource.isFolder()) {
                contentType = "text/directory";
            } else if (resource.isProject()) {
                contentType = "text/vnd.ideproject+directory";
            }
        }
        Item item = getVfsItem(resource);
        if (resource.isFolder()) {
            // To simplify working with cache just clear it when rename folder.
            cache.clear();
        } else {
            cache.remove(item.getId());
        }
        final String url = createUrl(item.getLinks().get(Link.REL_RENAME), Pair.of("newname", newname), Pair.of("mediaType", contentType));
        item = post(url, ItemDto.class, null, 200);
        cache.put(item.getId(), item);
        return createResource(resource.getParent(), item);
    }

    @Override
    public Resource move(Resource resource, Folder newparent) {
        Item item = getVfsItem(resource);
        if (resource.isFolder()) {
            // To simplify working with cache just clear it when move folder.
            cache.clear();
        } else {
            cache.remove(item.getId());
        }
        final String url = createUrl(item.getLinks().get(Link.REL_MOVE), Pair.of("parentId", newparent.getId()));
        item = post(url, ItemDto.class, null, 200);
        cache.put(item.getId(), item);
        return createResource(newparent, item);
    }

    @Override
    public Lock lock(File file, long timeout) {
        cache.remove(file.getId());
        final String url =
                createUrl(vfsInfo.getUrlTemplates().get(Link.REL_LOCK), Pair.of("id", file.getId()), Pair.of("timeout", timeout));
        return post(url, LockImpl.class, null, 200);
    }

    @Override
    public void unlock(File file, String lockToken) {
        cache.remove(file.getId());
        final String url =
                createUrl(vfsInfo.getUrlTemplates().get(Link.REL_UNLOCK), Pair.of("id", file.getId()), Pair.of("lockToken", lockToken));
        post(url, null, null, 204);
    }

    @Override
    public AccessControlList loadACL(Resource resource) {
        final String url = getVfsItem(resource).getLinks().get(Link.REL_ACL).getHref();
        final AccessControlEntryImpl[] vfsACL = get(url, AccessControlEntryImpl[].class, 200);
        final List<ResourceAccessControlEntry> acl = new ArrayList<>(vfsACL.length);
        for (AccessControlEntry e : vfsACL) {
            acl.add(new ResourceAccessControlEntryImpl(e));
        }
        return new AccessControlListImpl(this, acl, resource);
    }

    @Override
    public void updateACL(AccessControlList acl) {
        final List<ResourceAccessControlEntry> all = acl.getAll();
        if (all.isEmpty()) {
            return;
        }
        for (Iterator<ResourceAccessControlEntry> i = all.iterator(); i.hasNext(); ) {
            ResourceAccessControlEntry e = i.next();
            if (!e.isUpdated()) {
                i.remove();
            }
        }
        if (all.isEmpty()) {
            return;
        }

        final List<AccessControlEntry> vfsACL = new ArrayList<>(all.size());
        for (ResourceAccessControlEntry e : all) {
            vfsACL.add(new AccessControlEntryImpl(e.getPrincipal(), new HashSet<>(e.getPermissions())));
        }

        final Item item = getVfsItem(acl.getResource());
        final String url = item.getLinks().get(Link.REL_ACL).getHref();
        post(url, null, vfsACL, 204);
    }

    private String createUrl(Link link, Pair<String, ?>... parameters) {
        String url;
        try {
            url = URLDecoder.decode(link.getHref(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(); // not expected UTF-8 supported
        }
        if (parameters == null || parameters.length == 0) {
            return url;
        }
        final int length = url.length();
        final StringBuilder sb = new StringBuilder();
        int n = 0;
        int p = 0;
        int m;
        for (; ; ) {
            n = url.indexOf('[', n);
            if (n > 0) {
                m = url.indexOf(']', n);
                sb.append(url.substring(p, n));
            } else {
                sb.append(url.substring(p, length));
                break;
            }
            if (m > 0 && m < length) {
                final String replace = url.substring(n + 1, m);
                Object o = null;
                for (int i = 0, l = parameters.length; i < l && o == null; i++) {
                    if (replace.equals(parameters[i].first)) {
                        o = parameters[i].second;
                    }
                }
                if (o != null) {
                    sb.append(o);
                }
            }
            p = n = ++m;
        }
        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return url;
        }
    }

    public Item getVfsItem(Resource resource) {
        return getItemById(resource.getId());
    }

    private Item getItemById(String id) {
        Item item = cache.get(id);
        if (item == null) {
            final String url = createUrl(vfsInfo.getUrlTemplates().get(Link.REL_ITEM), Pair.of("id", id));
            cache.put(id, item = get(url, ItemDto.class, 200));
        }
        return item;
    }

    private Resource createResource(Folder parent, Item item) {
        switch (item.getItemType()) {
            case FILE:
                return new FileImpl(this, parent, item.getId(), item.getName());
            case FOLDER:
                return new Folder(this, parent, item.getId(), item.getName());
            case PROJECT:
                return new Project(this, parent, item.getId(), item.getName());
            default:
                return null;
        }
    }

    private <R> R get(String url, Class<R> responseType, int success, Pair<String, ?>... parameters) {
        try {
            final String str = doRequest(url, "GET", null, null, success, parameters);
            if (!(str == null || str.isEmpty())) {
                return JsonHelper.fromJson(str, responseType, null);
            }
            return null;
        } catch (IOException | JsonParseException e) {
            throw new VirtualFileSystemUnknownException(e);
        }
    }

    private <R> R post(String url, Class<R> responseType, Object body, int success, Pair<String, ?>... parameters) {
        try {
            final String str = doRequest(url, "POST", body, "application/json", success, parameters);
            if (!(str == null || str.isEmpty())) {
                return JsonHelper.fromJson(str, responseType, null);
            }
            return null;
        } catch (IOException | JsonParseException e) {
            throw new VirtualFileSystemUnknownException(e);
        }
    }

    private String doRequest(String url, String method, Object body, String contentType, int success, Pair<String, ?>... parameters)
            throws IOException {
        HttpURLConnection conn = null;
        try {
            if (parameters != null && parameters.length > 0) {
                final StringBuilder sb = new StringBuilder();
                sb.append(url);
                sb.append('?');
                for (int i = 0, l = parameters.length; i < l; i++) {
                    String name = URLEncoder.encode(parameters[i].first, "UTF-8");
                    String value = parameters[i].second == null ? null : URLEncoder.encode(String.valueOf(parameters[i].second), "UTF-8");
                    if (i > 0) {
                        sb.append('&');
                    }
                    sb.append(name);
                    if (value != null) {
                        sb.append('=');
                        sb.append(value);
                    }
                }
                url = sb.toString();
            }
            conn = (HttpURLConnection)new URL(url).openConnection();
            authenticate(conn);
            conn.setRequestMethod(method);
            conn.setInstanceFollowRedirects(true);
            if (body != null) {
                conn.addRequestProperty("content-type", contentType);
                conn.setDoOutput(true);
                try (OutputStream output = conn.getOutputStream()) {
                    output.write(JsonHelper.toJson(body).getBytes());
                }
            }
            final int responseCode = conn.getResponseCode();
            if (responseCode != success) {
                throw restoreRemoteException(responseCode, conn);
            }
            return IoUtil.readAndCloseQuietly(conn.getInputStream());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void authenticate(HttpURLConnection conn) {
        // TODO
    }

    private VirtualFileSystemAPIException restoreRemoteException(int responseCode, HttpURLConnection conn) {
        final int vfsExitCode = conn.getHeaderFieldInt("X-Exit-Code", -1);
        final InputStream in = conn.getErrorStream();
        String message = null;
        if (in != null) {
            try {
                message = IoUtil.readAndCloseQuietly(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        switch (vfsExitCode) {
            case ExitCodes.CONSTRAINT:
                throw new VirtualFileSystemAPIException(new ConstraintException(message));
            case ExitCodes.INVALID_ARGUMENT:
                throw new VirtualFileSystemAPIException(new InvalidArgumentException(message));
            case ExitCodes.ITEM_EXISTS:
                throw new VirtualFileSystemAPIException(new ItemAlreadyExistException(message));
            case ExitCodes.ITEM_NOT_FOUND:
                throw new VirtualFileSystemAPIException(new ItemNotFoundException(message));
            case ExitCodes.LOCK_CONFLICT:
                throw new VirtualFileSystemAPIException(new LockException(message));
            case ExitCodes.UNSUPPORTED:
                throw new VirtualFileSystemAPIException(new NotSupportedException(message));
            case ExitCodes.NOT_PERMITTED:
                throw new VirtualFileSystemAPIException(new PermissionDeniedException(message));
            case ExitCodes.INTERNAL_ERROR:
                throw new VirtualFileSystemAPIException(new VirtualFileSystemException(message));
            default:
                LOG.error(message);
                throw new VirtualFileSystemUnknownException(String.format("Invalid response status %d from remote server. ", responseCode));
        }
    }

    private InputStream bufferStream(InputStream in, int memThreshold) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[8192];
        int r;
        boolean overflow = false;
        while ((!overflow) && (r = in.read(b)) != -1) {
            bos.write(b, 0, r);
            overflow = bos.size() > memThreshold;
        }

        if (overflow) {
            java.io.File f = Files.createTempFile(null, null).toFile();

            FileOutputStream fos = new FileOutputStream(f);
            bos.writeTo(fos);
            while ((r = in.read(b)) != -1) {
                fos.write(b, 0, r);
            }
            fos.close();
            return new DeleteOnCloseFileInputStream(f);
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }
}
