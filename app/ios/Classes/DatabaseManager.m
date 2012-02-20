//
//  DatabaseManager.m
//  Carat
//
//  Created by Anand Padmanabha Iyer on 11/5/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "DatabaseManager.h"

@implementation DatabaseManager
{
    NSString *dbName;
    sqlite3 *dbHandle;
}

//
// Open the sqlite connection.
//
- (void) openDbConnection
{
    @try {
        sqlite3_open([dbName UTF8String], &dbHandle);
    }
    @catch (NSException *exception) {
        NSLog(@"openDbConnection: Caught %@: %@", [exception name], [exception reason]);
    }
}

//
// Close the sqlite connection.
//
- (void) closeDbConnection
{
    @try {
        sqlite3_close(dbHandle);
    }
    @catch (NSException *exception) {
        NSLog(@"closeDbConnection: Caught %@: %@", [exception name], [exception reason]);
    }
}

//
// Sometimes we might be forced to be terminated by iOS for memory consumption. 
// In that case, close and open the connection so that we release the memory 
// held by sqlite3 handler.
//
- (void) reopenDbConnection
{
    [self closeDbConnection];
    [self openDbConnection];
}

//
// 
// 

@end
