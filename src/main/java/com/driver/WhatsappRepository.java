package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository{
    private Map<String, User> userMap = new HashMap<>();//mobile - user
    private Map<Group, List<User>> groupListMap = new HashMap<>();//group - members
    private Map<Group, List<Message>> groupMessages = new HashMap<>();//group - messages
    private Map<Message, User> messageUserMap = new HashMap<>();//message - sender
    private Map<Group, User> adminMap = new HashMap<>();//group - admin
    private int numberOfGroups = 0;
    private int messageId = 0;

    public boolean isNewUser(String mobile){
        return userMap.containsKey(mobile);
    }
    public void addUser(String name, String mobile) {
        User user = new User(name, mobile);
        userMap.put(mobile, user);
    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        Group group = new Group();

        User admin = users.get(0);
        if(users.size() == 2){
            String groupName = users.get(1).getName();
            group.setName(groupName);
            group.setNumberOfParticipants(2);
            return group;
        }

        numberOfGroups++;
        String groupName = "Group "+numberOfGroups;
        group.setName(groupName);
        group.setNumberOfParticipants(users.size());
        groupListMap.put(group, users);
        adminMap.put(group, admin);

        return group;
    }

    public int createMessage(String content) {
        messageId++;
        Message message = new Message(messageId, content, new Date());
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!groupListMap.containsKey(group))
            throw new Exception("Group does not exist");
        if(!groupListMap.get(group).contains(sender))
            throw new Exception("You are not allowed to send message");

        List<Message> messages = groupMessages.getOrDefault(group, new ArrayList<>());
        messages.add(message);
        groupMessages.put(group, messages);
        messageUserMap.put(message, sender);

        return groupMessages.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.
        if(!groupListMap.containsKey(group))
            throw new Exception("Group does not exist");

        User admin = adminMap.get(group);
        if(!admin.equals(approver))
            throw new Exception("Approver does not have rights");

        if(!groupListMap.get(group).contains(user))
            throw new Exception("User is not a participant");

        adminMap.put(group, user);

        return "SUCCESS";
    }
}
