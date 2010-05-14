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

import java.util.HashMap;

import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Contains wrappers and utilities for VIX functions. The goal is to also make
 * these functions more usable in Java.
 * <p/>
 * Methods here should do the following:
 * <ul>
 * <li>Hide JNA complexities (i.e. dealing with pointers) as much as possible.</li>
 * <li>Perform error checking on jobs and throw Exceptions instead.</li>
 * </ul>
 * <p/>
 * For full accessibility to the API, call methods in {@link VixLibrary}
 * <p/>
 * Based on the VIX C library version 1.8.1
 * {@link http://www.vmware.com/support/developer/vix-api/vix18_reference/}
 */
public class VixUtils {

   public static VixLibrary mVix = VixLibrary.INSTANCE;

   /**
    * Helper method that waits for a VIX job to complete. Should only be used
    * for situations where you don't need to get anything from the job handle
    * (i.e. a host or VM handle).
    *
    * @param job
    *           handle for any VIX job
    * @param release
    *           Whether the job handle should be released after it has
    *           completed. The handle should not be released if properties need
    *           to be obtained from the job handle.
    * @throws VixException
    */
   public static void waitForJob(VixHandle job, boolean release)
         throws VixException {
      VixError err = mVix.VixJob_Wait(job, VixPropertyID.VIX_PROPERTY_NONE);
      checkError(err);
      if (release) {
         mVix.Vix_ReleaseHandle(job);
      }
   }

   /**
    * Helper method to obtain a result handle from a job handle. For example,
    * use this when obtaining a host or VM handle.
    * <p/>
    * Blocks for the job to complete before attempting to obtain the result
    * handle.
    *
    * @param jobHandle
    *           VixHandle for a job
    * @param releaseHandle
    *           <code>true</code> if the job handle should be released
    *           afterwards; <code>false</code> otherwise.
    * @return result handle
    * @throws VixException
    *            If the job resulted in an error
    */
   public static VixHandle getResultHandleFromJob(
         VixHandle jobHandle,
         boolean releaseHandle) throws VixException {
      VixHandleByReference href = new VixHandleByReference();
      VixError err =
         mVix.VixJob_Wait(jobHandle,
                          VixPropertyID.VIX_PROPERTY_JOB_RESULT_HANDLE,
                          href,
                          VixPropertyID.VIX_PROPERTY_NONE);
      checkError(err);
      return href.getValue();
   }

   /**
    * Creates a map of processes running in the given guest. Caller should have
    * previously called VixVM_LoginInGuest(). Map key is the pid, which contains
    * the value of another map with the keys 'name', 'owner', and 'command'.
    *
    * @param vmHandle
    *           Handle for a VM.
    * @return Map of processes
    * @throws VixException
    */
   public static HashMap<Long, HashMap<String, String>> getProcessesInGuest(
         VixHandle vmHandle) throws VixException {
      HashMap<Long, HashMap<String, String>> processMap =
            new HashMap<Long, HashMap<String, String>>();
      VixHandle jobHandle =
            mVix.VixVM_ListProcessesInGuest(vmHandle, 0, null, null);
      waitForJob(jobHandle, false);
      int num =
            mVix.VixJob_GetNumProperties(jobHandle,
                                         VixPropertyID.VIX_PROPERTY_JOB_RESULT_ITEM_NAME);
      for (int i = 0; i < num; i++) {
         PointerByReference procName = new PointerByReference();
         LongByReference pid = new LongByReference();
         PointerByReference owner = new PointerByReference();
         PointerByReference command = new PointerByReference();

         mVix.VixJob_GetNthProperties(jobHandle,
                                      i,
                                      VixPropertyID.VIX_PROPERTY_JOB_RESULT_ITEM_NAME,
                                      procName,
                                      VixPropertyID.VIX_PROPERTY_JOB_RESULT_PROCESS_ID,
                                      pid,
                                      VixPropertyID.VIX_PROPERTY_JOB_RESULT_PROCESS_OWNER,
                                      owner,
                                      VixPropertyID.VIX_PROPERTY_JOB_RESULT_PROCESS_COMMAND,
                                      command,
                                      VixPropertyID.VIX_PROPERTY_NONE);

         long key = pid.getValue();
         processMap.put(key, new HashMap<String, String>());
         processMap.get(key).put("name", procName.getValue().getString(0));
         processMap.get(key).put("owner", owner.getValue().getString(0));
         processMap.get(key).put("command", command.getValue().getString(0));

         mVix.Vix_FreeBuffer(pid.getPointer());
         mVix.Vix_FreeBuffer(procName.getPointer());
         mVix.Vix_FreeBuffer(owner.getPointer());
         mVix.Vix_FreeBuffer(command.getPointer());
      }
      mVix.Vix_ReleaseHandle(jobHandle);
      return processMap;
   }

   /**
    * Checks the given error and throws an exception with the corresponding
    * error message if needed.
    *
    * @param vixError
    *           Error to check
    * @throws VixException
    */
   public static void checkError(VixError vixError) throws VixException {
      if (!vixError.equals(VixError.VIX_OK)) {
         throw new VixException(vixError);
      }
   }

   /**
    * Gets the value of the specified environment variable. You must call
    * VixVM_LoginInGuest() before calling this method.
    * <p/>
    * Only supports user variables currently, not system level variables.
    *
    * @param vmHandle
    *           Handle for a VM
    * @param varName
    *           Name of the environment variable
    * @return Value of the variable if found; <code>null</code> otherwise.
    * @throws VixException
    */
   public static String getEnvironmentVariable(
         VixHandle vmHandle,
         String varName) throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_ReadVariable(vmHandle,
                                    VixConstants.VIX_GUEST_ENVIRONMENT_VARIABLE,
                                    varName,
                                    0,
                                    null,
                                    null);
      PointerByReference valPtr = new PointerByReference();
      mVix.VixJob_Wait(jobHandle,
                       VixPropertyID.VIX_PROPERTY_JOB_RESULT_VM_VARIABLE_STRING,
                       valPtr,
                       VixPropertyID.VIX_PROPERTY_NONE);
      mVix.Vix_ReleaseHandle(jobHandle);
      return valPtr.getValue().getString(0);
   }

   /**
    * Creates a directory inside of the given VM. You must call
    * VixVM_LoginInGuest() before calling this method.
    *
    * @param vmHandle
    *           Handle for a VM
    * @param dirPath
    *           Absolute path for the directory to be created
    * @throws VixException
    *            If there was a problem creating the directory other than
    *            VIX_E_ALREADY_EXISTS
    */
   public static void createDirectoryInGuest(VixHandle vmHandle, String dirPath)
         throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_CreateDirectoryInGuest(vmHandle,
                                              dirPath,
                                              VixHandle.VIX_INVALID_HANDLE,
                                              null,
                                              null);
      VixError err =
            mVix.VixJob_Wait(jobHandle, VixPropertyID.VIX_PROPERTY_NONE);
      if (!err.equals(VixError.VIX_E_ALREADY_EXISTS)) {
         throw new VixException(err);
      }
   }
}
