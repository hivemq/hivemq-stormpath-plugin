package com.dcsquare.hivemq.plugin.stormpathplugin.callbacks;

import com.dcsquare.hivemq.spi.security.ClientData;
import com.dcsquare.hivemq.spi.topic.MqttTopicPermission;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.group.Group;
import com.stormpath.sdk.group.GroupList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Lukas Brandl
 */
public class AuthorizationTest {

    @Mock
    ClientData clientData;

    @Mock
    Application app;

    @Mock
    Account account;

    @Mock
    GroupList groupList;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_topic_permissions() throws Exception {
        List<Group> groups = new LinkedList<Group>();
        List<String> topics = Arrays.asList("testtopicA", "testtopicB", "testtopicC");

        addMockGroupToList(groups, topics.get(0));
        addMockGroupToList(groups, topics.get(1));
        addMockGroupToList(groups, topics.get(2));

        MockIterator mockIterator = new MockIterator(groups);
        when(groupList.iterator()).thenReturn(mockIterator);

        when(account.getGroups()).thenReturn(groupList);
        TestAuthorization testAuthorization = new TestAuthorization(app, account);
        List<MqttTopicPermission> permissions = testAuthorization.getPermissionsForClient(clientData);

        assertEquals(3, permissions.size());

        for (MqttTopicPermission permission : permissions) {
            assertTrue(topics.contains(permission.getTopic()));
        }

    }

    @Test
    public void test_no_topic_permissions_on_empty_groups_for_account() throws Exception {
        List<Group> emptyGroupList = new LinkedList<Group>();
        MockIterator mockIterator = new MockIterator(emptyGroupList);
        when(groupList.iterator()).thenReturn(mockIterator);

        when(account.getGroups()).thenReturn(groupList);
        TestAuthorization testAuthorization = new TestAuthorization(app, account);
        List<MqttTopicPermission> permissions = testAuthorization.getPermissionsForClient(clientData);

        assertTrue(permissions.isEmpty());
    }

    @Test
    public void test_no_topic_permissions_on_null_account() throws Exception {
        TestAuthorization testAuthorization = new TestAuthorization(app, null);
        List<MqttTopicPermission> permissions = testAuthorization.getPermissionsForClient(clientData);

        assertTrue(permissions.isEmpty());
    }

    class TestAuthorization extends Authorization {

        private final Account account;

        TestAuthorization(Application application, Account account) {
            super(application);
            this.account = account;
        }

        @Override
        Account getAccount(ClientData clientData) {
            return account;
        }
    }

    class MockIterator implements Iterator<Group> {
        Iterator<Group> groupIterator;

        MockIterator(List<Group> groups) {
            groupIterator = groups.iterator();
        }

        @Override
        public boolean hasNext() {
            return groupIterator.hasNext();
        }

        @Override
        public Group next() {
            return groupIterator.next();
        }

        @Override
        public void remove() {
            fail("Remove method shouldn't be used");
        }
    }

    private void addMockGroupToList(List<Group> list, String topic) {
        Group group = mock(Group.class);
        when(group.getName()).thenReturn(topic);
        list.add(group);
    }

}
