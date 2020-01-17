package be.uantwerpen.sc.Messages;

/**
 * Type of workers
 */
public enum SimWorkerType {
    car,
    f1,
    drone;

    public static SimWorkerType StringToType(String type) {
        switch (type.toUpperCase()) {
            case("CAR"): return  SimWorkerType.car;
            case("F1"): return  SimWorkerType.f1;
            case("DRONE"): return  SimWorkerType.drone;
            default: throw new IllegalArgumentException("Unkwown worker type");
        }
    }

    //Added on 16/12/2019
    public static String TypeToString(SimWorkerType type){
        switch (type){
            case f1:
                return "F1";
            case car:
                return "CAR";
            case drone:
                return "DRONE";
            default: throw new IllegalArgumentException("Unkwown worker type");
        }
    }
}
