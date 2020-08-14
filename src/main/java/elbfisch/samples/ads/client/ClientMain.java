/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : ClientMain.java
 * VERSION   : -
 * DATE      : -
 * PURPOSE   : 
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 *
 * This file is part of the jPac process automation controller.
 * jPac is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jPac is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the jPac If not, see <http://www.gnu.org/licenses/>.
 */

package elbfisch.samples.ads.client;

import java.net.URI;
import org.jpac.CharString;
import org.jpac.Decimal;
import org.jpac.EventTimedoutException;
import org.jpac.ImpossibleEvent;
import org.jpac.InputInterlockException;
import org.jpac.Logical;
import org.jpac.Module;
import org.jpac.OutputInterlockException;
import org.jpac.PeriodOfTime;
import org.jpac.ProcessException;
import org.jpac.ShutdownRequestException;
import org.jpac.SignalInvalidException;
import org.jpac.SignedInteger;
import org.jpac.IoDirection;
import org.jpac.vioss.IoCharString;
import org.jpac.vioss.IoDecimal;
import org.jpac.vioss.IoLogical;
import org.jpac.vioss.IoSignedInteger;
import org.jpac.vioss.opcua.Handshake;

public class ClientMain extends Module{
    Logical       toggle;
    SignedInteger cmd;
    SignedInteger dintValue;
    Logical       flashLight;
    
    public ClientMain(){
        super(null, "Main");
        try{
        	Log.info("instantiating ...");
            toggle      = new IoLogical(this, "toggle", new URI("ads://192.168.0.99/Main.toggle"), IoDirection.INPUT);
        	Log.info("1st signal instantiated");
            cmd         = new IoSignedInteger(this, "lastCommand", new URI("ads://192.168.0.99/Main.cmd?size=1"), IoDirection.INPUT);
            dintValue   = new IoSignedInteger(this, "dintValue", new URI("ads://192.168.0.99/Main.dintValue"), IoDirection.OUTPUT);
            flashLight  = new IoLogical(this, "flashLight", new URI("ads://192.168.0.99/Main.flashLight"), IoDirection.OUTPUT);
        	Log.info("instantiated");
        }
        catch(Exception exc){
            Log.error("Error:", exc);
        };
    }

    @Override
    protected void work() throws ProcessException {
        int command         = 100;
        boolean flashLightb = false;
        
        PeriodOfTime delay = new PeriodOfTime(0.5 * sec);
        try{
            Log.info("started");
            //new ImpossibleEvent().await();
            while(true){
                try{
                    if (!cmd.isValid() && !toggle.isValid()){
                        //wait, until plc is connected properly
                        Log.info("awaiting signals getting valid ...");
                        cmd.becomesValid().and(toggle.becomesValid()).await();
                        Log.info("signals got valid");
                    }
                    dintValue.set(command++);
                    flashLightb = !flashLightb;
                    flashLight.set(flashLightb);
                    delay.await();
                }
                catch(ShutdownRequestException exc){
                    throw exc;
                }
                catch(EventTimedoutException | SignalInvalidException exc){
                    Log.error("handshake failed. Restarting protocol ...");
                }
                catch(Exception exc){
                    Log.error("Error", exc);
                }
            }
        }
        finally{
            Log.info("finished");
        }
    }

    @Override
    protected void preCheckInterlocks() throws InputInterlockException {
    }

    @Override
    protected void postCheckInterlocks() throws OutputInterlockException {
    }

    @Override
    protected void inEveryCycleDo() throws ProcessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
