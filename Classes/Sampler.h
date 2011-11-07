//
//  Sampler.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 11/5/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#ifndef Carat_Sampler_h
#define Carat_Sampler_h

#import "UIDeviceProc.h"

@interface Sampler : NSObject {
    NSManagedObjectModel *managedObjectModel;
    NSManagedObjectContext *managedObjectContext;
    NSPersistentStoreCoordinator *persistentStoreCoordinator;
}
- (void) sampleNow;
@end

#endif
