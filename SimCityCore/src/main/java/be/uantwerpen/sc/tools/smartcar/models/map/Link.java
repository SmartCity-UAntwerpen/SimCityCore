package be.uantwerpen.sc.tools.smartcar.models.map;

/**
 * Created by Niels on 24/03/2016.
 */
public class Link
{
    private Long id;
    private Long length;
    private String startDirection;
    private String stopDirection;
    private Point startPoint;
    private Point stopPoint;
    private int weight;
    //private int pointLock;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getLength()
    {
        return length;
    }

    public void setLength(Long length)
    {
        this.length = length;
    }

    public String getStartDirection()
    {
        return startDirection;
    }

    public void setStartDirection(String startDirection)
    {
        this.startDirection = startDirection;
    }

    public String getStopDirection()
    {
        return stopDirection;
    }

    public void setStopDirection(String stopDirection)
    {
        this.stopDirection = stopDirection;
    }

    public Point getStartPoint()
    {
        return startPoint;
    }

    public void setStartPoint(Point startPoint)
    {
        this.startPoint = startPoint;
    }

    public Point getStopPoint()
    {
        return stopPoint;
    }

    public void setStopPoint(Point stopPoint)
    {
        this.stopPoint = stopPoint;
    }

    public int getWeight()
    {
        return weight;
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
    }

    /*public int getPointLock() {
        return pointLock;
    }

    public void setPointlock(int pointlock)
    {
        this.pointLock = pointlock;
    }
*/
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link that = (Link) o;

        if (id != that.id)
            return false;

        if (length != null ? !length.equals(that.length) : that.length != null)
            return false;

        if (startDirection != null ? !startDirection.equals(that.startDirection) : that.startDirection != null)
            return false;

        if (stopDirection != null ? !stopDirection.equals(that.stopDirection) : that.stopDirection != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (int)(id % Integer.MAX_VALUE);

        result = 31 * result + (length != null ? length.hashCode() : 0);
        result = 31 * result + (startDirection != null ? startDirection.hashCode() : 0);
        result = 31 * result + (stopDirection != null ? stopDirection.hashCode() : 0);

        return result;
    }
}
