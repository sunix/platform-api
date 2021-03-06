/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.local;

import com.codenvy.api.account.server.dao.Account;
import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.server.dao.Member;
import com.codenvy.api.account.server.dao.Subscription;
import com.codenvy.api.account.shared.dto.Billing;
import com.codenvy.api.account.shared.dto.SubscriptionAttributes;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Eugene Voevodin
 */
@Singleton
public class LocalAccountDaoImpl implements AccountDao {
    @Override
    public void create(Account account) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Account getById(String id) {
        return new Account().withName("acc_name")
                            .withId(id)
                            .withAttributes(Collections.singletonMap("attribute1", "value"));
    }

    @Override
    public Account getByName(String name) {
        return new Account().withName(name)
                            .withId("acc0xffaassdeereqWsss")
                            .withAttributes(Collections.singletonMap("attribute1", "value"));
    }

    @Override
    public List<Account> getByOwner(String owner) {
        return Arrays.asList(new Account().withName("acc_name")
                                          .withId("acc0xffaassdeereqWsss")
                                          .withAttributes(Collections.singletonMap("attribute1", "value")));
    }

    @Override
    public void update(Account account) {
        throw new RuntimeException("Not implemented");

    }

    @Override
    public void remove(String id) {
        throw new RuntimeException("Not implemented");

    }

    @Override
    public void addMember(Member member) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void removeMember(Member member) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void addSubscription(Subscription subscription) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void removeSubscription(String subscriptionId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Subscription getSubscriptionById(String subscriptionId) {
        return DtoFactory.getInstance().createDto(Subscription.class)
                         .withId(subscriptionId)
                         .withPlanId("plan0xfffffffff")
                         .withServiceId("serviceId")
                         .withProperties(new HashMap<String, String>());
    }

    @Override
    public List<Subscription> getSubscriptions(String accountId, String serviceId) {
        return Arrays.asList(DtoFactory.getInstance().createDto(Subscription.class)
                                       .withId("Subscription0xfffffffff")
                                       .withPlanId("plan0xfffffffff")
                                       .withServiceId("serviceId")
                                       .withProperties(new HashMap<String, String>())
                            );
    }

    @Override
    public void updateSubscription(Subscription subscription) throws NotFoundException, ServerException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<Member> getMembers(String accountId) {
        return Arrays.asList(DtoFactory.getInstance().createDto(Member.class).withAccountId(accountId)
                                       .withUserId("userId122332133123").withRoles(Arrays.asList("account/owner")),
                             DtoFactory.getInstance().createDto(Member.class).withAccountId(accountId)
                                       .withUserId("userId112233322239").withRoles(Arrays.asList("account/member"))
                            );
    }

    @Override
    public List<Member> getByMember(String userId) {
        final Member member = new Member()
                .withAccountId("cc0xffaassdeereqWsss")
                .withUserId("userId122332133123")
                .withRoles(Arrays.asList("account/member"));
        return Arrays.asList(member);
    }

    @Override
    public List<Subscription> getSubscriptions() throws ServerException {
        return Arrays.asList(DtoFactory.getInstance().createDto(Subscription.class)
                                       .withId("Subscription0xfffffffff")
                                       .withPlanId("plan0xfffffffff")
                                       .withServiceId("serviceId")
                                       .withProperties(new HashMap<String, String>())
                            );
    }

    @Override
    public void saveSubscriptionAttributes(String subscriptionId, SubscriptionAttributes subscriptionAttributes) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public SubscriptionAttributes getSubscriptionAttributes(String subscriptionId) throws ServerException {
        return DtoFactory.getInstance().createDto(SubscriptionAttributes.class)
                         .withTrialDuration(5)
                         .withStartDate("10/21/2015")
                         .withEndDate("10/21/2015")
                         .withDescription("description")
                         .withCustom(Collections.singletonMap("key", "value"))
                         .withBilling(DtoFactory.getInstance().createDto(Billing.class)
                                                .withStartDate("10/21/2015")
                                                .withEndDate("10/21/2015")
                                                .withUsePaymentSystem("true")
                                                .withPaymentToken("token")
                                                .withContractTerm(12)
                                                .withCycle(1)
                                                .withCycleType(2));
    }

    @Override
    public void removeSubscriptionAttributes(String subscriptionId) throws ServerException, NotFoundException {
        throw new RuntimeException("Not implemented");
    }
}
