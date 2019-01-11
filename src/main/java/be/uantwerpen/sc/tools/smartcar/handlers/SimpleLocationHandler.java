package be.uantwerpen.sc.tools.smartcar.handlers;

/**
 * Deze klasse overschijft de problematische methods van de echte LocationHandler en voorziet dummy-data
 * Dit levert een betere simulatie op dan wanneer de LocationHandler in de war geraakt.
 *
 * Dit is meestal het gevolg van een bocht van 180° of een parkeersequentie
 */
public class SimpleLocationHandler extends LocationHandler {

    private boolean rfidUsed = false;

    public SimpleLocationHandler() {
        super();
        logger.warn("Using simple location handler. Location data will be inaccurate");
    }

    public void startFollowLine() {
        this.destinationDistance = 300; // we assume the length of 1 tile
        this.driving = true;
    }

    public String getNodeRFID(int nodeID) {
        if(!rfidUsed) {
            rfidUsed = true;
            return super.getNodeRFID(nodeID);
        }
        else {
            // Allow only use on startpoint because we don't know at a later time
            logger.warn("RFID read ignored because simple simulator is used.");
            return "";
        }
    }

    public void updatePosTurn(float angle)  {
        // Do nothing. Result isn't used directly
    }

    public void updatePosDrive() {
        // Do nothing. Result isn't used directly
    }

}
