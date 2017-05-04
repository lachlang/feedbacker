'use strict';

describe('service [Session]', function() {
	
	var session, $httpBackend;

    beforeEach(module('feedbacker.services'));
    
    beforeEach(inject(function(_Session_, _$httpBackend_) {

    	session = _Session_;
    	$httpBackend = _$httpBackend_;
    }));
    
    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
      });
    
    it('can get an instantce of itself', function(){
    	expect(session).toBeDefined();
    });
    
    it('has defined functions', function() {
        expect(angular.isFunction(session.login)).toBe(true);
        expect(angular.isFunction(session.logout)).toBe(true);
        expect(angular.isFunction(session.validSession)).toBe(true);
        expect(angular.isFunction(session.isLeader)).toBe(true);
        expect(angular.isFunction(session.isAdmin)).toBe(true);
    });

    describe("calls the appropriate server api", function() {
        var dummyResult = "dummyResult";
    	
    	it('to login a user', function() {
    		var result, promise = session.login("user","pass");

            $httpBackend.expectPUT('/api/session/login', '{"apiVersion":"1.0","body":{"username":"user","password":"pass"}}').respond(200, dummyResult);
    		
    		// set the response value
    		promise.then(function(data) {
    			result = data.data;
    		});
    		expect(result).toBeUndefined(); // it really should at this point
    		$httpBackend.flush();

    		expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
    	});

        it('to logout a user', function() {
            var result, promise = session.logout();

            $httpBackend.expectGET('/api/session/logout').respond(200, dummyResult);
            
            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined();
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

    });

    describe("has the correct in state when", function() {

        var doLogin = function() {
            expect(session.validSession()).toBe(false);

            session.login("user","pass");
            $httpBackend.expectPUT('/api/session/login',
                '{"apiVersion":"1.0","body":{"username":"user","password":"pass"}}',
                function(headers) { return headers['Cookies'] = 'FEEDBACKER_SESSION=abcdef'}
                ).respond(200, "dummyResult");
            $httpBackend.flush();

//            expect(session.validSession()).toBe(true);
        };

        it('is initiated', function() {
            expect(session.validSession()).toBe(false);
        });

        xit('logging in a user', function() {
            doLogin();
        });

        xit('logging out a user', function() {
            doLogin();

            session.logout();
            $httpBackend.expectGET('/api/session/logout').respond(200, "dummyResult");
            $httpBackend.flush();

            expect(session.validSession()).toBe(false);
        });

    });
});