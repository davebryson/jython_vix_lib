/*******************************************************************************
 * Copyright (c) 2009 VMware, Inc. licensed under the terms of the BSD. All
 * other rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - Neither the name of VMware, Inc. nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.vmware.vix.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.vmware.vix.VixConstants;
import com.vmware.vix.VixException;
import com.vmware.vix.VixUtils;
import com.vmware.vix.VixVSphereHandle;
import com.vmware.vix.VixVmHandle;

/**
 * Sample program that finds all VMs registered and running on the given host.
 * Processes running within the VM are then printed to the screen.
 */
public class FindRunningVms {

   /**
    * Main entry point for execution.
    * Assumes an ESX host.
    *
    * @param args
    *    user
    *    password - password for host and VMs
    *    host - hostname/ip of the target VIX host
    */
   public static void main(String[] args) {
      VixVSphereHandle hostHandle = null;

      String user;
      String password;
      String host;
      if (args.length < 3) {
         throw new RuntimeException("Not enough arguments.");
      } else {
         user = args[0];
         password = args[1];
         host = args[2];
      }

      try {
         System.out.println("Connecting to host...");

         hostHandle = new VixVSphereHandle(host, user, password);
         ArrayList<String> vmxPaths = hostHandle.getRunningVms();

         /*
          * Now connect to each one of the VM's and list which processes are
          * running inside of the guest.
          */
         if (vmxPaths.size() > 0) {
            for (String vmxPath : vmxPaths) {
               try {
                  System.out.println("Connecting to VM " + vmxPath + ".");
                  VixVmHandle vmHandle = hostHandle.openVm(vmxPath);
                  vmHandle.loginInGuest("Administrator",
                                        password,
                                        VixConstants.VIX_LOGIN_IN_GUEST_REQUIRE_INTERACTIVE_ENVIRONMENT);
                  HashMap<Long, HashMap<String, String>> procMap =
                        VixUtils.getProcessesInGuest(vmHandle);
                  Set<Long> pids = procMap.keySet();
                  for (Long pid : pids) {
                     String name = procMap.get(pid).get("name");
                     String cmd = procMap.get(pid).get("pid");
                     System.out.printf("\t%d\t%s\t%s\n", pid, name, cmd);
                  }
                  vmHandle.logoutFromGuest();
                  vmHandle.release();
               } catch (Exception e) {
                  System.err.println(e);
                  e.printStackTrace();
               }
            }
         } else {
            System.err.println("No running VMs were found!");
         }
      } catch (VixException e) {
         System.err.println(e);
         e.printStackTrace();
      } catch (Exception e) {
         System.err.println(e);
         e.printStackTrace();
      } finally {
         System.err.println("Disconnecting from host.");
         hostHandle.disconnect();
      }
   }
}