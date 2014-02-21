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
package com.codenvy.api.factory;

import com.codenvy.api.factory.dto.AdvancedFactoryUrl;
import com.codenvy.api.factory.dto.Link;
import com.codenvy.api.factory.dto.Variable;
import com.codenvy.api.factory.dto.WelcomePage;

import java.util.*;
import java.util.concurrent.TimeUnit;

/** Implementation of {@link com.codenvy.api.factory.dto.AdvancedFactoryUrl} */
public class AdvancedFactoryUrlImpl extends SimpleFactoryUrlImpl implements AdvancedFactoryUrl {
    private String id;
    private String style;
    private String description;
    private String contactmail;
    private String author;
    private String userid;
    private Long     validuntil = TimeUnit.DAYS.toMillis(3650) + System.currentTimeMillis(); //10 * 365 = 10 years
    private Long     validsince = System.currentTimeMillis();
    private Long     created    = System.currentTimeMillis();
    private List<Link> links      = Collections.emptyList();
    private WelcomePage welcome;

    public AdvancedFactoryUrlImpl() {
        super();
    }

    public AdvancedFactoryUrlImpl(String version, String vcs, String vcsUrl, String commitId, String action, String openFile,
                                  boolean vcsInfo, String orgid, String affiliateid, String vcsBranch,
                                  Map<String, String> projectAttributes,
                                  List<Variable> variables) {
        super(version, vcs, vcsUrl, commitId, action, openFile, vcsInfo, orgid, affiliateid, vcsBranch, projectAttributes, variables);
    }

    public AdvancedFactoryUrlImpl(AdvancedFactoryUrl originFactory, List<Link> links) {
        super(originFactory.getV(), originFactory.getVcs(), originFactory.getVcsurl(),
              originFactory.getCommitid(), originFactory.getAction(), originFactory.getOpenfile(), originFactory.getVcsinfo(),
              originFactory.getOrgid(), originFactory.getAffiliateid(), originFactory.getVcsbranch(), originFactory.getProjectattributes(),
              originFactory.getVariables());

        id = originFactory.getId();
        style = originFactory.getStyle();
        description = originFactory.getDescription();
        contactmail = originFactory.getContactmail();
        author = originFactory.getAuthor();
        userid = originFactory.getUserid();
        validuntil = originFactory.getValiduntil();
        validsince = originFactory.getValidsince();
        created = originFactory.getCreated();
        welcome = originFactory.getWelcome();

        setLinks(links);
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactmail() {
        return contactmail;
    }

    public void setContactmail(String contactMail) {
        this.contactmail = contactMail;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public List<Link> getLinks() {
        return Collections.unmodifiableList(links);
    }

    public void setLinks(List<Link> links) {
        this.links = links;
        if (links != null) {
            this.links = new ArrayList<Link>(links);
        }
    }

    public Long getValiduntil() {
        return validuntil;
    }

    public void setValiduntil(Long validuntil) {
        this.validuntil = validuntil;
    }

    public Long getValidsince() {
        return validsince;
    }

    public void setValidsince(Long validsince) {
        this.validsince = validsince;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public WelcomePage getWelcome() {
        return welcome;
    }

    public void setWelcome(WelcomePage welcome) {
        this.welcome = welcome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AdvancedFactoryUrlImpl that = (AdvancedFactoryUrlImpl)o;

        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (contactmail != null ? !contactmail.equals(that.contactmail) : that.contactmail != null) return false;
        if (created != null ? !created.equals(that.created) : that.created != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (links != null ? !links.equals(that.links) : that.links != null) return false;
        if (style != null ? !style.equals(that.style) : that.style != null) return false;
        if (userid != null ? !userid.equals(that.userid) : that.userid != null) return false;
        if (validsince != null ? !validsince.equals(that.validsince) : that.validsince != null) return false;
        if (validuntil != null ? !validuntil.equals(that.validuntil) : that.validuntil != null) return false;
        if (welcome != null ? !welcome.equals(that.welcome) : that.welcome != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (style != null ? style.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (contactmail != null ? contactmail.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (userid != null ? userid.hashCode() : 0);
        result = 31 * result + (validuntil != null ? validuntil.hashCode() : 0);
        result = 31 * result + (validsince != null ? validsince.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        result = 31 * result + (welcome != null ? welcome.hashCode() : 0);
        return result;
    }
}
