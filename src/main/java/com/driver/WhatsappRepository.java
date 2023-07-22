package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository{
    private Map<String, User> userMap = new HashMap<>();//mobile - user
    private Map<Group, List<User>> groupListMap = new HashMap<>();//group - members
    private Map<Group, List<Message>> groupMessages = new HashMap<>();//group - messages
    private Map<User, List<Message>> userMessages = new HashMap<>();//sender - messages
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

        List<Message> usersMesagesList = userMessages.getOrDefault(sender, new ArrayList<>());
        usersMesagesList.add(message);
        userMessages.put(sender, usersMesagesList);

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

    public int removeUser(User user) throws Exception {
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group +
        // the updated number of messages in group +
        // the updated number of overall messages)

        if(!userMap.containsKey(user.getMobile()))
            throw new Exception("User not found");

        if(adminMap.containsKey(user))
            throw new Exception("Cannot remove admin");

        Group group = null;
        for(Group group1 : groupListMap.keySet()){
            List<User> users = groupListMap.get(group1);
            if(ifExistInGroup(user, users)){
                group = group1;
                break;
            }
        }

        List<Message> groupMessage = groupMessages.get(user);
        for(Message message : userMessages.get(user)){
            groupMessage.remove(message);
            messageId--;
        }//removing messages in group
        groupMessages.put(group, groupMessage);

        userMap.remove(user.getMobile());

        List<User> users = groupListMap.get(group);
        users.remove(user);
        groupListMap.put(group, users);
        group.setNumberOfParticipants(users.size());

        int updatedNumberOfUser = groupListMap.get(group).size();
        int updatedMessages = groupMessages.get(group).size();

        return updatedNumberOfUser + updatedMessages + messageId;
    }
    public boolean ifExistInGroup(User user, List<User> list){
        for(User u : list){
            if(user.equals(u))
                return true;
        }
        return false;
    }

}
