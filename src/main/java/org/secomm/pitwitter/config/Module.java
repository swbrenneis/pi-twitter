package org.secomm.pitwitter.config;

import java.util.List;

public class Module {

    private String name;

    private List<FollowContext> following;

    public Module() {
    }

    public Module(String name, List<FollowContext> following) {
        this.name = name;
        this.following = following;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FollowContext> getFollowing() {
        return following;
    }

    public void setFollowing(List<FollowContext> following) {
        this.following = following;
    }

    @Override
    public String toString() {
        return "Follow{" +
                "name='" + name + '\'' +
                ", following=" + following +
                '}';
    }
}
