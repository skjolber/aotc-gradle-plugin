package com.github.skjolber.aotc.agent;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Provide virtual machine diagnostics using DiagnosticCommandMBean
 * that comes with the Oracle HotSpot JVM.
 * 
 */
public class VirtualMachineDiagnostics {
	
	//  @see https://github.com/dustinmarx/javautilities/blob/master/dustin/utilities/diagnostics/VirtualMachineDiagnostics.java

   /** Object Name of DiagnosticCommandMBean. */
   public final static String DIAGNOSTIC_COMMAND_MBEAN_OBJECT_NAME =
      "com.sun.management:type=DiagnosticCommand";

   /** My MBean Server. */
   private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

   /** Platform MBean Server. */
   private final ObjectName objectName;

   /**
    * Create an instance of me with the provided object name.
    *
    * @param newObjectName ObjectName associated with DiagnosticCommand MBean.
    */
   private VirtualMachineDiagnostics(final ObjectName newObjectName)
   {
      this.objectName = newObjectName;
   }

   /**
    * Only publicly available method for instantiating an instance
    * of me.
    *
    * @return An instance of me.
    */
   public static VirtualMachineDiagnostics newInstance()
   {
      try {
         final ObjectName objectName = new ObjectName(DIAGNOSTIC_COMMAND_MBEAN_OBJECT_NAME);
         return new VirtualMachineDiagnostics(objectName);
      } catch (MalformedObjectNameException badObjectNameEx) {
         throw new RuntimeException("Unable to create an ObjectName and so unable to create instance of VirtualMachineDiagnostics");
      }
   }

   /**
    * Provide touched methods as single String.
    *
    * @return Single string containing formatted thread dump.
    */
   public String getPrintTouchedMethods() {
      return invokeNoStringArgumentsCommand("vmPrintTouchedMethods", "Touched Methods");
   }   

   /**
    * Invoke operation on the DiagnosticCommandMBean that accepts
    *    String array argument but does not require any String
    *    argument and returns a String.
    *
    * @param operationName Name of operation on DiagnosticCommandMBean.
    * @param operationDescription Description of operation being invoked
    *    on the DiagnosticCommandMBean.
    * @return String returned by DiagnosticCommandMBean operation.
    */
   private String invokeNoStringArgumentsCommand(String operationName, String operationDescription) {
      try {
         return (String) server.invoke(objectName, operationName, new Object[] {null}, new String[]{String[].class.getName()});
      }
      catch (InstanceNotFoundException | ReflectionException | MBeanException exception) {
    	  System.err.println("ERROR: Unable to access '" + operationDescription + "' - " + exception);
      }
      return null;
   }

}