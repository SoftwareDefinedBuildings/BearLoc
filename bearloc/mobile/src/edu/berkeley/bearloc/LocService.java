/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Author: Kaifei Chen <kaifei@eecs.berkeley.edu>
 */

package edu.berkeley.bearloc;

import java.util.UUID;

import org.json.JSONObject;

public interface LocService {
    /**
     * Get location for this device.
     * 
     * @param listener
     *            listener of the event that location is returned by server
     * 
     * @return true if success, else false
     */
    public abstract boolean getLocation(LocListener listener);

    /**
     * Get location for this device with given id at given time.
     * 
     * @param id
     *            UUID of target device
     * @param time
     *            UNIX timestamp in millisecond for location query
     * @param listener
     *            listener of the event that location is returned by server
     * 
     * @return true if success, else false
     */
    // TODO Currently unimplemented
    public abstract boolean getLocation(UUID id, Long time, LocListener listener);

    /**
     * Report any type of data on this phone to server. Currently only those
     * specified in specification will be processed.
     * 
     * @param type
     *            type of data to be posted
     * @param data
     *            data to be posted
     * 
     * @return true if success, else false
     */
    public abstract boolean postData(String type, JSONObject data);

    /**
     * Get candidates of locations at the lowest level in given location.
     * 
     * @param loc
     *            location for query
     * @param targetSem
     *            the semantic of which the list of candidate is
     * @param listener
     *            listener of the event that candidate is returned by server
     * 
     * @return true if success, else false
     */
    public abstract boolean getCandidate(JSONObject loc,
            CandidateListener listener);
}
