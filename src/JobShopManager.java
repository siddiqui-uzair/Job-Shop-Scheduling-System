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
            tryReleaseJobs();
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

            tryReleaseJobs();

            // machine waits here until it is assigned to a completed job
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

    private void tryReleaseJobs() {   // Try to release jobs in queue order while enough machines are available
        while (!pendingJobs.isEmpty()) {
            Job nextJob = pendingJobs.get(0);

            if (!canSatisfy(nextJob)) {
                return;
            }

            releaseJob(nextJob);
            pendingJobs.remove(0);
        }
    }

    // to  check weaather all machines types needed by a job are currently waiting
    private boolean canSatisfy(Job job) {
        Map<String, Integer> requiredMachines = countRequiredMachines(job);

        for (Map.Entry<String, Integer> entry : requiredMachines.entrySet()) {
            String machineType = entry.getKey();
            int requiredCount = entry.getValue();

            Queue<MachineRequest> queue = waitingMachines.get(machineType);
            int availableCount = (queue == null) ? 0 : queue.size();

            if (availableCount < requiredCount) {
                return false;
            }
        }

        return true;
    }

    // asssign the required waiting machines to this job and wake them up
    private void releaseJob(Job job) {
        Map<String, Integer> requiredMachines = countRequiredMachines(job);

        for (Map.Entry<String, Integer> entry : requiredMachines.entrySet()) {
            String machineType = entry.getKey();
            int requiredCount = entry.getValue();

            Queue<MachineRequest> queue = waitingMachines.get(machineType);

            for (int i = 0; i < requiredCount; i++) {
                MachineRequest request = queue.remove();
                request.assignedJobName = job.jobName;
                request.released = true;
                request.condition.signal();
            }
        }
    }

    // to count how many machines of each type a job needs
    private Map<String, Integer> countRequiredMachines(Job job) {
        Map<String, Integer> requiredMachines = new HashMap<>();

        for (Operation operation : job.operations) {
            String machineType = operation.machineType;
            int currentCount = requiredMachines.getOrDefault(machineType, 0);
            requiredMachines.put(machineType, currentCount + 1);
        }

        return requiredMachines;
    }
}