'use strict';

describe('service [Session]', function() {
	
	var session, $httpBackend, $cookies, $location;

    beforeEach(module('feedbacker.services'));
    
    beforeEach(inject(function(_Session_, _$httpBackend_, _$cookies_, _$location_) {

    	session = _Session_;
    	$httpBackend = _$httpBackend_;

    	$cookies = _$cookies_;
    	spyOn($cookies, 'get');
    	spyOn($cookies, 'remove');

    	$location = _$location_;
    	spyOn($location, 'path');
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

      it('should validate a session', function() {
        session.validSession();

        expect($cookies.get).toHaveBeenCalledWith("FEEDBACKER_SESSION");
      });
    });

    describe("has the correct state when", function() {

        var doLogin = function() {
            expect(session.validSession()).toBe(false);

            session.login("user","pass");
            $httpBackend.expectPUT('/api/session/login',
                '{"apiVersion":"1.0","body":{"username":"user","password":"pass"}}',
                function(headers) { return headers['Cookies'] = 'FEEDBACKER_SESSION=abcdef'}
                ).respond(200, "dummyResult");
            $httpBackend.flush();

            expect(session.validSession()).toBe(true);
        };

        it('is initiated', function() {
            expect(session.validSession()).toBe(false);
        });

        it('logging in a user', function() {

            session.login("user","pass");
            $httpBackend.expectPUT('/api/session/login',
                '{"apiVersion":"1.0","body":{"username":"user","password":"pass"}}',
                function(headers) { return headers['Cookies'] = 'FEEDBACKER_SESSION=abcdef'}
                ).respond(200, "dummyResult");
            $httpBackend.flush();
        });

        it('logging out a user', function() {
            session.logout();

            expect($cookies.remove).toHaveBeenCalledWith("FEEDBACKER_SESSION");
            expect($location.path).toHaveBeenCalledWith("#/landing");
            $httpBackend.expectGET('/api/session/logout').respond(200, "dummyResult");
            $httpBackend.flush();
        });

    });
});