/*******************************************************************************
 * Copyright (c) 2010 VMware, Inc. licensed under the terms of the BSD. All
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
package com.vmware.vix.util;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.vmware.vix.VixConstants;
import com.vmware.vix.VixError;
import com.vmware.vix.VixException;
import com.vmware.vix.VixHandle;
import com.vmware.vix.VixLibrary;
import com.vmware.vix.VixPropertyID;
import com.vmware.vix.VixUtils;
import com.vmware.vix.VixVmHandle;

/**
 * Contains static utility methods for manipulating files within a guest.
 */
public class GuestFileUtil {

   private static VixLibrary mVix = VixLibrary.INSTANCE;

   public static final int SYMLINK_FILE =
         VixConstants.VIX_FILE_ATTRIBUTES_SYMLINK;

   public static final int SYMLINK_DIRECTORY =
         VixConstants.VIX_FILE_ATTRIBUTES_SYMLINK
               + VixConstants.VIX_FILE_ATTRIBUTES_DIRECTORY;

   /**
    * Get the size of a given file.
    *
    * @param vmHandle
    *           Handle for the VM containing filePath.
    * @param filePath
    *           Absolute path to a file that exists in the guest.
    * @return File size.
    * @throws VixException
    */
   public static long getFileSize(VixVmHandle vmHandle, String filePath)
         throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_GetFileInfoInGuest(vmHandle, filePath, null, null);
      VixUtils.waitForJob(jobHandle, false);

      LongByReference fileSizeRef = new LongByReference();
      VixError error =
            mVix.Vix_GetProperties(jobHandle,
                                   VixPropertyID.VIX_PROPERTY_JOB_RESULT_FILE_SIZE,
                                   fileSizeRef,
                                   VixPropertyID.VIX_PROPERTY_NONE);
      VixUtils.checkError(error);

      return fileSizeRef.getValue();
   }

   /**
    * Get file attribute flags for a file in a guest.
    *
    * @param vmHandle
    *           Handle for the VM containing filePath.
    * @param filePath
    *           Absolute path to a file that exists in the guest.
    * @return File attribute flags.
    * @throws VixException
    */
   public static int getFileFlags(VixVmHandle vmHandle, String filePath)
         throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_GetFileInfoInGuest(vmHandle, filePath, null, null);
      VixUtils.waitForJob(jobHandle, false);

      IntByReference fileFlagsRef = new IntByReference();
      VixError error =
            mVix.Vix_GetProperties(jobHandle,
                                   VixPropertyID.VIX_PROPERTY_JOB_RESULT_FILE_FLAGS,
                                   fileFlagsRef,
                                   VixPropertyID.VIX_PROPERTY_NONE);
      VixUtils.checkError(error);

      return fileFlagsRef.getValue();
   }

   /**
    * Get the modification time for a file in a guest.
    *
    * @param vmHandle
    *           Handle for the VM containing filePath.
    * @param filePath
    *           Absolute path to a file that exists in the guest.
    * @return The modification time of the file or directory in seconds since
    *         the epoch.
    * @throws VixException
    */
   public static long getModTime(VixVmHandle vmHandle, String filePath)
         throws VixException {
      VixHandle jobHandle =
            mVix.VixVM_GetFileInfoInGuest(vmHandle, filePath, null, null);
      VixUtils.waitForJob(jobHandle, false);

      LongByReference modTimeRef = new LongByReference();
      VixError error =
            mVix.Vix_GetProperties(jobHandle,
                                   VixPropertyID.VIX_PROPERTY_JOB_RESULT_FILE_MOD_TIME,
                                   modTimeRef,
                                   VixPropertyID.VIX_PROPERTY_NONE);
      VixUtils.checkError(error);

      return modTimeRef.getValue();
   }

   /**
    * Tests whether the given file path is a directory.
    *
    * @param vmHandle
    *           Handle for the VM containing filePath.
    * @param filePath
    *           Absolute path to a file in the guest.
    * @return <code>true</code> if the path represents a directory;
    *         <code>false</code> otherwise.
    * @throws VixException
    */
   public static boolean isDirectory(VixVmHandle vmHandle, String filePath)
         throws VixException {
      int fileFlags = getFileFlags(vmHandle, filePath);
      return fileFlags == VixConstants.VIX_FILE_ATTRIBUTES_DIRECTORY
            || fileFlags == SYMLINK_DIRECTORY;
   }

   /**
    * Tests whether the given file path represents a file (not a directory or
    * device).
    *
    * @param vmHandle
    *           Handle for the VM containing filePath.
    * @param filePath
    *           Absolute path to a file in the guest.
    * @return <code>true</code> if the path represents a file;
    *         <code>false</code> otherwise.
    * @throws VixException
    */
   public static boolean isFile(VixVmHandle vmHandle, String filePath)
         throws VixException {
      int fileFlags = getFileFlags(vmHandle, filePath);
      return fileFlags == SYMLINK_FILE
            || fileFlags != VixConstants.VIX_FILE_ATTRIBUTES_DIRECTORY;
   }

   /**
    * Tests whether the given file path represents a symbolic link.
    *
    * @param vmHandle
    *           Handle for the VM containing filePath.
    * @param filePath
    *           Absolute path to a file in the guest.
    * @return <code>true</code> if the path represents a symbolic link;
    *         <code>false</code> otherwise.
    * @throws VixException
    */
   public static boolean isSymbolicLink(VixVmHandle vmHandle, String filePath)
         throws VixException {
      int fileFlags = getFileFlags(vmHandle, filePath);
      return fileFlags == VixConstants.VIX_FILE_ATTRIBUTES_SYMLINK
            || fileFlags == SYMLINK_DIRECTORY || fileFlags == SYMLINK_FILE;
   }

}