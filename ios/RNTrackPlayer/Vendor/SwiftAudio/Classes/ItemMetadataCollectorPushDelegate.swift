//
//  ItemMetadataCollectorPushDelegate.swift
//  RNTrackPlayer
//
//  Created by Jakub Perzylo on 02/06/2021.
//  Copyright © 2021 David Chavez. All rights reserved.
//

import Foundation
import AVFoundation

class ActiveSpeakerEvent {
    var id: String
    var activeSpeaker: String
    var date: Date
    
    init(id: String, activeSpeaker: String, date: Date) {
        self.id = id
        self.activeSpeaker = activeSpeaker
        self.date = date
    }
}

class ItemMetadataCollectorPushDelegate: NSObject, AVPlayerItemMetadataCollectorPushDelegate {
    let player: AVPlayer
    var awaitingDateRangeEvents: [ActiveSpeakerEvent] = []
    var registeredEvents: Set<String> = []
    
    public init(player: AVPlayer) {
        self.player = player
    }
    
    func metadataCollector(_ metadataCollector: AVPlayerItemMetadataCollector,
                           didCollect metadataGroups: [AVDateRangeMetadataGroup],
                           indexesOfNewGroups: IndexSet,
                           indexesOfModifiedGroups: IndexSet) {
  
        for metadataGroup in metadataGroups {
            let eventId = metadataGroup.uniqueID ?? ""
            if registeredEvents.contains(eventId) {
                continue
            }
            //print("Seeing", eventId, "for the first time, registering date", metadataGroup.startDate)
            self.registeredEvents.insert(eventId)
            
            let event = ActiveSpeakerEvent(id: eventId, activeSpeaker: "", date: metadataGroup.startDate)
        
            for metadata in metadataGroup.items {
                if (metadata.identifier?.rawValue == "lsdr/X-ACTIVE-SPEAKER") {
                    event.activeSpeaker = metadata.stringValue!
                }
            }
            self.awaitingDateRangeEvents.append(event)
        }
    }
}

