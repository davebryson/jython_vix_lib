/*******************************************************************************
 * Copyright (c) 2009-2010 VMware, Inc. licensed under the terms of the BSD. All
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.vmware.vix.util.GuestFileUtil;

/**
 * Class representing a VIX VM. Contains wrappers for VixVM_* functions.
 */
@SuppressWarnings("serial")
public class VixVmHandle extends VixHandle {

   private final VixLibrary mVix = VixLibrary.INSTANCE;

   /**
    * Constructor.
    */
   public VixVmHandle() {
      super();
   }

   /**
    * @see VixHandle#VixHandle(long)
    */
   public VixVmHandle(long val) {
      super(val);
   }

   /**
    * Copies the given file on the guest to the specified location on the client
    * machine. File paths must be fully specified (no variables).
    *
    * @param srcFile
    *           Absolute path of file to copy on the guest.
    * @param destFile
    *           Absolute path for file's destination on the client.
    * @throws VixException
    */
   public void copyFileFromGuestToHost(String srcFile, String destFile)
         throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_CopyFileFromGuestToHost(this,
                                               srcFile,
                                               destFile,
                                               0,
                                               VixHandle.VIX_INVALID_HANDLE,
                                               null,
                                               null);
      VixUtils.waitForJob(jobHandle, true);
   }

   /**
    * Copies the given file on the host to the specified location on the guest.
    * File paths must be fully specified (no variables).
    *
    * @param srcFile
    *           Absolute path of file to copy on the host.
    * @param destFile
    *           Absolute path for file's destination on the guest.
    * @throws VixException
    */
   public void copyFileFromHostToGuest(String srcFile, String destFile)
         throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_CopyFileFromHostToGuest(this,
                                               srcFile,
                                               destFile,
                                               0,
                                               VixHandle.VIX_INVALID_HANDLE,
                                               null,
                                               null);
      VixUtils.waitForJob(jobHandle, true);
   }

   /**
    * Creates a directory in the guest operating system. If the directory
    * already exists, the error associated with the job handle will be set to
    * VIX_E_FILE_ALREADY_EXISTS. If the parent directories for the specified
    * path do not exist, this function will create them.
    *
    * @param dirPath
    *           The path to the directory to be created.
    * @throws VixException
    */
   public void createDirectoryInGuest(String dirPath) throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_CreateDirectoryInGuest(this,
                                              dirPath,
                                              VIX_INVALID_HANDLE,
                                              null,
                                              null);
      VixUtils.waitForJob(jobHandle, true);
   }

   /**
    * Creates a temporary file in the guest operating system.
    *
    * @param options
    *           Must be 0.
    * @return Path to the temporary file which was created in the guest.
    * @throws VixException
    */
   public String createTempFileInGuest(int options) throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_CreateTempFileInGuest(this,
                                             options,
                                             VIX_INVALID_HANDLE,
                                             null,
                                             null);

      /*
       * Get the location of the temp file which was created.
       */
      PointerByReference valPtr = new PointerByReference();
      VixError err =
            mVix.VixJob_Wait(jobHandle,
                             VixPropertyID.VIX_PROPERTY_JOB_RESULT_ITEM_NAME,
                             valPtr,
                             VixPropertyID.VIX_PROPERTY_NONE);
      mVix.Vix_ReleaseHandle(jobHandle);
      VixUtils.checkError(err);
      return valPtr.getValue().getString(0);
   }

   /**
    * This function deletes a directory in the guest operating system. Any files
    * or subdirectories in the specified directory will also be deleted.
    *
    * @param dirPath
    *           The absolute path to the directory to be deleted.
    * @param options
    *           Must be 0.
    * @throws VixException
    */
   public void deleteDirectoryInGuest(String dirPath, int options)
         throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_DeleteDirectoryInGuest(this,
                                              dirPath,
                                              options,
                                              null,
                                              null);
      VixUtils.waitForJob(jobHandle, true);
   }

   /**
    * Deletes a file in the guest operating system.
    *
    * @param filePath
    *           The absolute path to the file to be deleted.
    * @throws VixException
    */
   public void deleteFileInGuest(String filePath) throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_DeleteFileInGuest(this, filePath, null, null);
      VixUtils.waitForJob(jobHandle, true);
   }

   /**
    * Checks whether the given directory exists within the guest.
    *
    * @param dirPath
    *           Absolute path to the directory in the guest to be checked.
    * @return <code>true</code> if the supplied path represents a directory that
    *         exists; <code>false</code> otherwise.
    * @throws VixException
    */
   public boolean directoryExistsInGuest(String dirPath) throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_DirectoryExistsInGuest(this, dirPath, null, null);
      return guestObjectExists(jobHandle);
   }

   /**
    * Checks whether the given file exists inside of the guest. If the file
    * is a directory, this will return <code>false</code>.
    *
    * @param filePath
    *           Absolute path for a file (no variables).
    * @return <code>true</code> if the file exists; <code>false</code>
    *         otherwise.
    * @throws VixException
    */
   public boolean fileExistsInGuest(String filePath) throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_FileExistsInGuest(this, filePath, null, null);
      return guestObjectExists(jobHandle);
   }

   /**
    * Get the value of an environment variable in the guest. Requires a call to
    * login() first.
    * <p/>
    * Only user variables are visible.
    *
    * @param varName
    *           environment variable to get
    * @return Value of the variable if found; <code>null</code> otherwise.
    * @throws VixException
    */
   public String getEnvironmentVariable(String varName) throws VixException {
      return readVariable(varName, VixConstants.VIX_GUEST_ENVIRONMENT_VARIABLE);
   }

   /**
    * Get value for a GuestVariable
    *
    * @param varName
    *           variable to get value of
    * @return Value of the variable if found; <code>null</code> otherwise.
    * @throws VixException
    */
   public String getGuestVariable(String varName) throws VixException {
      return readVariable(varName, VixConstants.VIX_VM_GUEST_VARIABLE);
   }

   /**
    * Get the IP address of this VM.
    *
    * @return IP address if found; <code>null</code> otherwise.
    * @throws VixException
    */
   public String getIpAddress() throws VixException {
      return getGuestVariable("ip");
   }

   /**
    * Get the display name of this VM.
    *
    * @return display name
    * @throws VixException
    */
   public String getName() throws VixException {
      return readVariable("displayName",
                          VixConstants.VIX_VM_CONFIG_RUNTIME_ONLY);
   }

   /**
    * Gets a listing of files in the given directory. Will not recurse through
    * sub-directories.
    *
    * @param dirPath
    *           Absolute path of a directory in the guest (no variables).
    * @return List of file names with absolute path. Empty if the directory has
    *         no files.
    * @throws VixException
    */
   public ArrayList<String> listDirectoryInGuest(String dirPath)
         throws VixException {
      return listDirectoryInGuest(dirPath, false);
   }

   /**
    * Gets a listing of files in the given directory. Will recurse through
    * sub-directories if specified.
    *
    * @param dirPath
    *           Absolute path of a directory in the guest (no variables).
    * @param recurse
    *           <code>true</code> if files should be examined in child
    *           directories.
    * @return List of file names (not absolute). Empty if the directory has no
    *         files.
    * @throws VixException
    */
   public ArrayList<String> listDirectoryInGuest(
         String dirPath,
         boolean recurse) throws VixException {
      String pathSeparator = getPathSeparatorFromPath(dirPath);
      ArrayList<String> files = new ArrayList<String>();
      VixHandle jobHandle =
            mVix.VixVM_ListDirectoryInGuest(this, dirPath, 0, null, null);
      VixUtils.waitForJob(jobHandle, false);
      int numFiles =
            mVix.VixJob_GetNumProperties(jobHandle,
                                         VixPropertyID.VIX_PROPERTY_JOB_RESULT_ITEM_NAME);

      VixError err;
      String joinedPath;
      String fileName;
      for (int i = 0; i < numFiles; i++) {
         PointerByReference fname = new PointerByReference();
         err =
               mVix.VixJob_GetNthProperties(jobHandle,
                                            i,
                                            VixPropertyID.VIX_PROPERTY_JOB_RESULT_ITEM_NAME,
                                            fname,
                                            VixPropertyID.VIX_PROPERTY_NONE);
         VixUtils.checkError(err);
         if (fname.getValue() != null) {
            fileName = fname.getValue().getString(0);

            /*
             * Check the file. If it is a directory, recurse through it and
             * its subdirectories.
             */
            joinedPath = dirPath + pathSeparator + fileName;
            if (GuestFileUtil.isDirectory(this, joinedPath) && recurse) {
               files.addAll(listDirectoryInGuest(joinedPath,
                                                 recurse));
            } else {
               files.add(joinedPath);
            }
         }
         mVix.Vix_FreeBuffer(fname.getPointer());
      }

      return files;
   }

   /**
    * Logs into the guest. Blocks until login completes.
    *
    * @param username
    *           username inside the guest OS
    * @param password
    * @param options
    *           Zero or
    *           {@link VixConstants#VIX_LOGIN_IN_GUEST_REQUIRE_INTERACTIVE_ENVIRONMENT}
    * @throws VixException
    */
   public void loginInGuest(String username, String password, int options) throws VixException {
      VixHandle jobHandle =
         mVix.VixVM_LoginInGuest(this,
                                username,
                                password,
                                options,
                                null,
                                null);
      VixUtils.waitForJob(jobHandle, true);
   }

   /**
    * Logs out from the guest. Safe to call even if login() has not been called.
    *
    * @throws VixException
    */
   public void logoutFromGuest() throws VixException {
      VixHandle jobHandle = mVix.VixVM_LogoutFromGuest(this, null, null);
      VixUtils.waitForJob(jobHandle, true);
   }

   /**
    * Get a guest variable value.
    *
    * @param varName
    *           Name of the variable.
    * @param varType
    *           One of:
    *           <ul>
    *           <li>VixConstants.VIX_VM_GUEST_VARIABLE</li>
    *           <li>VixConstants.VIX_VM_CONFIG_RUNTIME_ONLY</li>
    *           <li>VIX_GUEST_ENVIRONMENT_VARIABLE</li>
    *           </ul>
    * @return Value of the variable
    * @throws VixException
    */
   public String readVariable(String varName, int varType) throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_ReadVariable(this,
                                    varType,
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
      if (valPtr.getValue() != null) {
         return valPtr.getValue().getString(0);
      } else {
         return null;
      }
   }

   /**
    * Runs a script inside of the guest. Environment variables may be used in
    * the script, assuming that the interpreter will have access to these
    * variables.
    *
    * @param interpreter
    *           Optional full path to a script interpreter (i.e.
    *           c:\perl\bin\perl.exe). <code>null</code> to use cmd.exe in
    *           windows.
    * @param scriptText
    *           Text of the script to run; commands separated by newlines.
    * @param returnImmediately
    *           Whether method should return as soon as script starts.
    * @return exit code of the script; will be zero if
    *         <code>returnImmediately</code> is <code>true</code>.
    * @throws VixException
    */
   public int runScriptInGuest(
         String interpreter,
         String scriptText,
         boolean returnImmediately) throws VixException {
      VixRunProgramOptions options =
            returnImmediately ? VixRunProgramOptions.VIX_RUNPROGRAM_RETURN_IMMEDIATELY
                  : VixRunProgramOptions.NONE;
      VixHandle jobHandle =
            mVix.VixVM_RunScriptInGuest(this,
                                        interpreter,
                                        scriptText,
                                        options,
                                        VixHandle.VIX_INVALID_HANDLE,
                                        null,
                                        null);
      IntByReference exitCode = new IntByReference();
      VixError err =
            mVix.VixJob_Wait(jobHandle,
                             VixPropertyID.VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_EXIT_CODE,
                             exitCode,
                             VixPropertyID.VIX_PROPERTY_NONE);
      jobHandle.release();
      VixUtils.checkError(err);
      return exitCode.getValue();
   }

   /**
    * Wait for tools to start inside of the guest. Requires VM to be powered on.
    *
    * @param timeout
    *           The timeout in seconds. If VMware Tools has not started by this
    *           time, the function completes with an error. If the value of this
    *           argument is zero or negative, then there will be no timeout.
    * @throws VixException
    *            If tools does not start within the specified timeout.
    */
   public void waitForToolsInGuest(int timeout) throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_WaitForToolsInGuest(this, timeout, null, null);
      VixUtils.waitForJob(jobHandle, true);
   }

   /**
    * Get the path separator to use given a file path.
    *
    * @param filePath
    *           Path to be checked.
    * @return Either a Windows or Linux path separator depending on the path.
    */
   private String getPathSeparatorFromPath(String filePath) {
      String windowsSeparator = "\\";
      String linuxSeparator = "/";

      if (filePath.matches("^[a-zA-Z]:")) {
         /*
          * This is definitely a Windows path. Matches a Windows drive.
          */
         return windowsSeparator;
      } else if (filePath.contains(windowsSeparator)) {
         return windowsSeparator;
      } else {
         return linuxSeparator;
      }
   }

   /**
    * Convenience method which checks a job handle with a GUEST_OBJECT_EXISTS
    * job result. The job handle WILL be closed.
    *
    * @param jobHandle
    *           Job handle to be checked then closed.
    * @return <code>true</code> if the object exists; <code>false</code>
    *         otherwise.
    * @throws VixException
    */
   private boolean guestObjectExists(VixHandle jobHandle) throws VixException {
      IntByReference exists = new IntByReference();
      VixError err =
            mVix.VixJob_Wait(jobHandle,
                             VixPropertyID.VIX_PROPERTY_JOB_RESULT_GUEST_OBJECT_EXISTS,
                             exists,
                             VixPropertyID.VIX_PROPERTY_NONE);
      jobHandle.release();
      VixUtils.checkError(err);
      return exists.getValue() == 1;
   }
}