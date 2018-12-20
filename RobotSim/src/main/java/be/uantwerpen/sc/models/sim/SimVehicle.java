package be.uantwerpen.sc.models.sim;

import be.uantwerpen.sc.configurations.SpringContext;
import be.uantwerpen.sc.services.MapService;
import org.springframework.context.ApplicationContext;

/**
 * Created by Thomas on 5/05/2017.
 * SimVehicle subclass from SimBot
 * Adds Startpoint & Sim speed
 */
public abstract class SimVehicle extends SimBot
{
    protected int startPoint;
    protected long simSpeed;

    public SimVehicle(String name, int startPoint, long simSpeed)
    {
        super(name);
        this.type = "vehicle";
        this.startPoint = startPoint;
        this.simSpeed = simSpeed;
    }

    public void setStartPoint(int startPoint)
    {
        this.startPoint = startPoint;
    }

    @Override
    public int getStartPoint() {
        return this.startPoint;
    }

    public void setSimSpeed(int simSpeed)
    {
        this.simSpeed = simSpeed;
    }

    public Long getSimSpeed() {
        return this.simSpeed;
    }

    @Override
    public boolean parseProperty(String property, String value) throws Exception
    {
        if(super.parseProperty(property, value))
            return true;//Property already parsed

        switch(property.toLowerCase().trim())
        {
            case "speed":
                try {
                    int speed = Integer.parseInt(value);
                    this.setSimSpeed(speed);
                    return true;
                }
                catch(Exception e) {
                    throw new Exception("Could not parse value for speed setting! " + e.getMessage());
                }
            case "startpoint":
                try {
                    if(value.equals("auto")) {
                        return automaticStartPoint();
                    }
                    else {
                        int startPoint = Integer.parseInt(value);
                        this.setStartPoint(startPoint);
                        return true;
                    }
                }
                catch(Exception e) {
                    throw new Exception("Could not parse value for start point setting! " + e.getMessage());
                }
            default:
                return false;
        }
    }

    @Override
    public boolean parseProperty(String property) throws Exception
    {
        if(super.parseProperty(property))
            return true;

        switch(property.toLowerCase().trim())
        {
            case "speed":
                return true;
            case "startpoint":
                return true;
            default:
                return false;
        }
    }


    @Override
    public boolean printProperty(String property)
    {
        if(super.printProperty(property))
            return true;

        switch(property.toLowerCase().trim())
        {
            case "speed":
                System.out.println(String.valueOf(simSpeed) + "\n");
                return true;
            case "startpoint":
                System.out.println(String.valueOf(startPoint) + "\n");
                return true;
            default:
                return false;
        }
    }

    private boolean automaticStartPoint() {
        System.out.println("Setting starting point automatically...");

        //Get values from spring
        ApplicationContext context =  SpringContext.getAppContext();
        MapService service = context.getBean(MapService.class);

        return false;
    }
}
