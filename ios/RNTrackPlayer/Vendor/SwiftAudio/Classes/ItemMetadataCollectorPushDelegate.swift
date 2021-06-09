//
//  ItemMetadataCollectorPushDelegate.swift
//  RNTrackPlayer
//
//  Created by Jakub Perzylo on 02/06/2021.
//  Copyright © 2021 David Chavez. All rights reserved.
//

import Foundation
import AVFoundation

class InPlaylistEvent {
    var id: String
    var startDate: Date
    var attributes: [String: String]
    
    init(id: String, startDate: Date, attributes: [String: String]) {
        self.id = id
        self.startDate = startDate
        self.attributes = attributes
    }
}

class ItemMetadataCollectorPushDelegate: NSObject, AVPlayerItemMetadataCollectorPushDelegate {
    let player: AVPlayer
    var awaitingDateRangeEvents: [InPlaylistEvent] = []
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
            self.registeredEvents.insert(eventId)
            
            var attributes: [String: String] = [:]
            for metadata in metadataGroup.items {
                if let key = metadata.key?.debugDescription {
                    attributes[key] = metadata.stringValue!
                }
            }
            
            let event = InPlaylistEvent(id: eventId, startDate: metadataGroup.startDate, attributes: attributes)
    
        
            self.awaitingDateRangeEvents.append(event)
        }
    }
}

