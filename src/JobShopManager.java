import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class JobShopManager implements JobShopInterface {

    private final String mode;
    private final ReentrantLock lock;

    private final Map<String, Queue<MachineRequest>> waitingMachines;
    private final List<Job> pendingJobs;

    private static class MachineRequest {
        final String machineType;
        final int machineID;
        final Condition condition;

        String assignedJobName;
        boolean released;

        MachineRequest(String machineType, int machineID, Condition condition) {
            this.machineType = machineType;
            this.machineID = machineID;
            this.condition = condition;
            this.assignedJobName = null;
            this.released = false;
        }
    }

    public JobShopManager(String mode) {
        this.mode = mode;
        this.lock = new ReentrantLock();
        this.waitingMachines = new HashMap<>();
        this.pendingJobs = new ArrayList<>();
    }

    @Override
    public void specifyJobs(List<Job> jobs) {
        lock.lock();
        try {
            pendingJobs.addAll(jobs);
            } finally {
                lock.unlock();
            }
    }

    @Override
    public String thisMachineAvailable(String type, int ID) {
        lock.lock();
        try {
            MachineRequest request = new MachineRequest(type, ID, lock.newCondition());
            Queue<MachineRequest> queue =
            waitingMachines.computeIfAbsent(type, key -> new LinkedList<>());
            queue.add(request);

        while (!request.released) {
            try {
                request.condition.await();
                } catch (InterruptedException e) {
                }
            }

        return request.assignedJobName;
        } finally {
            lock.unlock();
        }
    }
}