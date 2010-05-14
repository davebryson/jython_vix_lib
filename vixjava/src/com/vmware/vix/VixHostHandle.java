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
package com.vmware.vix;

import java.util.ArrayList;
import java.util.Vector;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Class representing a connection with a VIX host. This includes the following
 * types of hosts:
 * <ul>
 * <li>VMware vSphere (ESX, vCenter)</li>
 * <li>VMware Server</li>
 * <li>VMware Workstation</li>
 * </ul>
 * <p/>
 * While this class can be used for a connection to any type of VIX host, it is
 * recommended that a subclass be used for the specific type of host.
 */
public class VixHostHandle extends VixHandle {

   private final VixLibrary mVix = VixLibrary.INSTANCE;

   /**
    * Constructor.
    */
   public VixHostHandle() {
      super();
   }

   /**
    * @see VixHandle#VixHandle(long)
    */
   public VixHostHandle(long val) {
      super(val);
   }

   /**
    * Constructor to establish a connection with a VIX host.
    *
    * @param apiVersion
    *           Version of the API. Should always be
    *           {@link VixConstants#VIX_API_VERSION}.
    * @param hostType
    *           Type of host to connect to (Workstation, ESX, Server)
    * @param hostName
    *           hostname/IP of the host. If this is a vSphere host, use a URL
    *           format: https://<hostname>:<port>/sdk
    * @param hostPort
    *           port on the host; ignored if this is a vSphere host.
    * @param userName
    *           user on the host
    * @param password
    *           password for the user
    * @throws VixException
    *            If there was a problem connecting with the host
    */
   public VixHostHandle(int apiVersion,
                        VixServiceProvider hostType,
                        String hostName,
                        int hostPort,
                        String userName,
                        String password) throws VixException {
    
       VixHandle jobHandle =
	   mVix.VixHost_Connect(apiVersion,
				hostType,
				hostName,
				hostPort,
				userName,
				password,
				VixHostOptions.NONE,
				VixHandle.VIX_INVALID_HANDLE,
				null,
				null);
       VixHandle handle = VixUtils.getResultHandleFromJob(jobHandle, true);
       super.setValue(handle.longValue());
   }

   /**
    * Get object with VIX library functions.
    *
    * @return VixLibrary Object with VIX functions
    */
   public VixLibrary getVix() {
      return mVix;
   }

   /**
    * Disconnect from the host.
    */
   public void disconnect() {
      if (!this.equals(VixHandle.VIX_INVALID_HANDLE)) {
         mVix.VixHost_Disconnect(this);
         mVix.Vix_ReleaseHandle(this);
         super.setValue(VixHandle.VIX_INVALID_HANDLE.longValue());
      }
   }

   /**
    * Get the list of VMX paths for VMs that are registered with this host.
    * These can be used to obtain handles for VMs.
    *
    * @return List of VMX paths. Will be empty if no VMs are found.
    * @throws VixException
    */
   public ArrayList<String> getRegisteredVms() throws VixException {
      return getVms(VixFindItemType.VIX_FIND_REGISTERED_VMS);
   }

   /**
    * Get the list of VMX paths for VMs that are currently running on this host.
    * These can be used to obtain handles for VMs.
    *
    * @return List of VMX paths. Will be empty if no VMs are found.
    * @throws VixException
    */
   public ArrayList<String> getRunningVms() throws VixException {
      return getVms(VixFindItemType.VIX_FIND_RUNNING_VMS);
   }

   /**
    * Gets all VMs matching the given VixFindItemType (running or registered).
    *
    * @param findType
    *           Type of VMs to find (running or registered)
    * @return List of VMX paths.
    * @throws VixException
    */
   private ArrayList<String> getVms(VixFindItemType findType) throws VixException {
      FindVmCallback finder = new FindVmCallback();
      VixHandle jobHandle =
            getVix().VixHost_FindItems(this,
                                       findType,
                                       VixHandle.VIX_INVALID_HANDLE,
                                       -1, // must always be -1
                                       finder,
                                       null);
      VixUtils.waitForJob(jobHandle, true);
      return finder.getVmxPaths();
   }

   /**
    * Gets a handle for the given VM.
    *
    * @param vmxPath
    *           full path to the VM's .vmx file
    * @return Handle for the VM
    * @throws VixException
    */
   public VixVmHandle openVm(String vmxPath) throws VixException {
      VixHandle jobHandle = getVix().VixVM_Open(this, vmxPath, null, null);
      VixHandle result = VixUtils.getResultHandleFromJob(jobHandle, true);
      return new VixVmHandle(result.longValue());
   }

   /**
    * Finds a VM on the host by the given name.
    *
    * @param vmName
    *           name of the VM to search for
    * @return Handle to the VM if found; <code>null</code> otherwise.
    * @throws VixException
    */
   public VixVmHandle findVmByName(String vmName) throws VixException {
      ArrayList<String> vmList = getRegisteredVms();
      for (String vmxPath : vmList) {
         VixVmHandle vmHandle = openVm(vmxPath);
         String name = vmHandle.getName();
         if (name != null && name.equalsIgnoreCase(vmName)) {
            return vmHandle;
         } else {
            mVix.Vix_ReleaseHandle(vmHandle);
         }
      }
      return null;
   }

   /**
    * Helper class to find VMs on this host.
    */
   private class FindVmCallback implements VixEventProc {

      private final ArrayList<String> mVmxPaths = new ArrayList<String>();

      /**
       * @see com.vmware.vix.VixEventProc#callbackProc(int, int, int,
       *      com.sun.jna.Pointer)
       */
      public void callback(
            int handle,
            int eventType,
            int moreEventInfo,
            Pointer clientData) {
         if (eventType != VixEventType.VIX_EVENTTYPE_FIND_ITEM.longValue()) {
            // Skip status updates
            return;
         }
         try {
            /*
             * Create a pointer so we can get the found VM's vmx path from VIX.
             */
            PointerByReference pref = new PointerByReference();
            VixError err =
                  getVix().Vix_GetProperties(new VixHandle(moreEventInfo),
                                             VixPropertyID.VIX_PROPERTY_FOUND_ITEM_LOCATION,
                                             pref,
                                             VixPropertyID.VIX_PROPERTY_NONE);
            VixUtils.checkError(err);
            String vmxPath = pref.getValue().getString(0);
            mVmxPaths.add(vmxPath);
            getVix().Vix_FreeBuffer(pref.getPointer());
         } catch (VixException e) {
            System.err.println("Unexpected exception occurred while searching for VMs."
                  + e);
            e.printStackTrace();
         }
      }

      /**
       * Gets the list of discovered VMX paths.
       *
       * @return List of VMX paths for registered VMs.
       */
      public ArrayList<String> getVmxPaths() {
         return mVmxPaths;
      }
   }
}