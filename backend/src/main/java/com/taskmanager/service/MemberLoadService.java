package com.taskmanager.service;

import com.taskmanager.model.MemberLoad;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.storage.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemberLoadService {

    @Value("${task.overload.threshold:5}")
    private int overloadThreshold;

    @Autowired
    private DataStorage dataStorage;

    public List<MemberLoad> getMemberLoads() {
        Map<String, MemberLoad> loadMap = new HashMap<>();
        for (Task task : dataStorage.getTasks().values()) {
            if (task.getAssignee() == null || task.getStatus() == TaskStatus.COMPLETED) {
                continue;
            }
            String assignee = task.getAssignee();
            MemberLoad load = loadMap.get(assignee);
            if (load == null) {
                load = new MemberLoad();
                load.setMemberName(assignee);
                load.setTaskCount(0);
                load.setTotalEstimatedDays(0);
                loadMap.put(assignee, load);
            }
            load.setTaskCount(load.getTaskCount() + 1);
            if (task.getEstimatedDays() != null) {
                load.setTotalEstimatedDays(load.getTotalEstimatedDays() + task.getEstimatedDays());
            }
        }
        List<MemberLoad> result = new ArrayList<>(loadMap.values());
        for (MemberLoad load : result) {
            load.setOverload(load.getTaskCount() > overloadThreshold);
        }
        return result;
    }

    public List<String> getOverloadedMembers() {
        List<String> overloaded = new ArrayList<>();
        for (MemberLoad load : getMemberLoads()) {
            if (load.getOverload()) {
                overloaded.add(load.getMemberName());
            }
        }
        return overloaded;
    }

    public MemberLoad getMemberLoad(String memberName) {
        for (MemberLoad load : getMemberLoads()) {
            if (load.getMemberName().equals(memberName)) {
                return load;
            }
        }
        return null;
    }
}
