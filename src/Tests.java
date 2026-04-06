import java.util.List;

public class Tests {


        public class MachineThread extends Thread {  // thread that simulates a machine becoming available in the shop
                public final String machineType;
                public final int machineID;
                private final JobShopManager jobShopManager;
                public String returnedJobName; // stores the job name returned when this machine is released

                public MachineThread(JobShopManager jobShopManager, String machineType, int machineID) {
                        this.jobShopManager = jobShopManager;
                        this.machineType = machineType;
                        this.machineID = machineID;
                        this.setName("Machine-" + machineType + "-" + machineID);
                }

                @Override
                public void run() {  // save the returned job name so later tests can check it
                        returnedJobName = jobShopManager.thisMachineAvailable(machineType, machineID);
                        
                        if (returnedJobName == null) {
                                        System.out.println(machineType + " " + machineID + " machine proceeding with null job name");
                        } else {
                                        System.out.println(machineType + " " + machineID + " machine proceeding for "  + returnedJobName);
                }
        }
}

        // UR2 example test
        public void exampleUR2Test() {
                //Map<String, Integer> expectedResult = Map.of("FDM",5,"SLA",1);
                JobShopManager jobShopManager = new JobShopManager("FCFS");

                System.out.println("\nStart the machines: \n");
                //Sart three machines of type FDM and two SLA:
                for (int i=1; i<=6; i++) new MachineThread(jobShopManager, "FDM", i).start();
                for (int i=1; i<=2; i++) new MachineThread(jobShopManager, "SLA", i).start();
                try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();} //to allow machine threads to start and run

                //Specify job 1
                Job job1 = new Job(
                                "Job1",
                                List.of(new Operation("FDM", 5),
                                        new Operation("FDM", 3),
                                        new Operation("FDM", 3),
                                        new Operation("SLA", 3)));

                //Specify job 2
                Job job2 = new Job(
                                "Job2",
                                List.of(new Operation("FDM", 5),
                                        new Operation("FDM", 3)));

                //Print out and submit jobs
                System.out.println("\nSpecify the Jobs ()   (and note that processing time is not used in FCFS)");
                System.out.println(job1);
                System.out.println(job2);
                jobShopManager.specifyJobs(List.of(job1, job2));
                //Allow job specifier to run and release machines
                try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

                System.out.println("\nNow examine the machines released:\n"
                        + "\tThe correct result is that five FDM machines and one SLA machine proceed.\n\t"
                        + "The specific machine IDs may vary between runs depending on thread scheduling.\n\t"
                        + "The printed job names should match Job1 or Job2 for released machines.\n"
                );
        }

        public void jobsBeforeMachinesTest() {
                JobShopManager jobShopManager = new JobShopManager("FCFS");

        Job job1 = new Job(
                "Job1",
                List.of(new Operation("FDM", 5),
                        new Operation("FDM", 3),
                        new Operation("SLA", 3)));

        Job job2 = new Job(
                "Job2",
                List.of(new Operation("FDM", 5),
                        new Operation("FDM", 3)));

        System.out.println("\nSubmit jobs before starting machines:\n");
        System.out.println(job1);
        System.out.println(job2);
        jobShopManager.specifyJobs(List.of(job1, job2));

        try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

        System.out.println("\nNow start the machines:\n");

        MachineThread[] fdmThreads = new MachineThread[6];
        MachineThread[] slaThreads = new MachineThread[2];

        for (int i = 0; i < 6; i++) {
                fdmThreads[i] = new MachineThread(jobShopManager, "FDM", i + 1);
                fdmThreads[i].start();
        }

        for (int i = 0; i < 2; i++) {
                slaThreads[i] = new MachineThread(jobShopManager, "SLA", i + 1);
                slaThreads[i].start();
        }

        try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

        int releasedFdm = 0;
        int releasedSla = 0;

        for (MachineThread thread : fdmThreads) {
                if (thread.returnedJobName != null) {
                        releasedFdm++;
                }
        }

        for (MachineThread thread : slaThreads) {
                if (thread.returnedJobName != null) {
                        releasedSla++;
                }
        }

        System.out.println("\nReleased counts:");
        System.out.println("FDM: " + releasedFdm);
        System.out.println("SLA: " + releasedSla);

        int job1Count = 0;
        int job2Count = 0;

        for (MachineThread thread : fdmThreads) {
                if ("Job1".equals(thread.returnedJobName)) {
                        job1Count++;
                } else if ("Job2".equals(thread.returnedJobName)) {
                        job2Count++;
                }
        }

        for (MachineThread thread : slaThreads) {
                if ("Job1".equals(thread.returnedJobName)) {
                        job1Count++;
                } else if ("Job2".equals(thread.returnedJobName)) {
                        job2Count++;
                }
        }

        System.out.println("Job1 assignments: " + job1Count);
        System.out.println("Job2 assignments: " + job2Count);

        System.out.println("\nExpected result:\n"
                + "\tFour FDM machines and one SLA machine should proceed.\n\t"
                + "The printed job names should be Job1 or Job2.\n");
}

        public void shortestJobFirstTest() {
                JobShopManager jobShopManager = new JobShopManager("SJF");
                
                Job longJob = new Job(
                        "LongJob",
                        List.of(new Operation("FDM", 8),
                                new Operation("SLA", 7)));

                Job shortJob = new Job(
                        "ShortJob",
                        List.of(new Operation("FDM", 2)));

        System.out.println("\nSubmit SJF jobs:\n");
        System.out.println(longJob);
        System.out.println(shortJob);
        jobShopManager.specifyJobs(List.of(longJob, shortJob));

        MachineThread fdmThread = new MachineThread(jobShopManager, "FDM", 1);
        fdmThread.start();

        try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

        System.out.println("\nExpected result:\n"
                + "\tOnly one FDM machine should proceed.\n\t"
                + "It should be released for ShortJob first.\n");
        }
}

