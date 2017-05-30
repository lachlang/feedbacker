'use strict';

describe('service [Account]', function() {
	
	var account, $httpBackend;

    beforeEach(module('feedbacker.services'));
    
    beforeEach(inject(function(_Account_, _$httpBackend_) {

    	account = _Account_;
    	$httpBackend = _$httpBackend_;
    }));
    
    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
      });
    
    it('can get an instantce of itself', function(){
    	expect(account).toBeDefined();
    });
    
    it('has defined functions', function() {
        expect(angular.isFunction(account.register)).toBe(true);
        expect(angular.isFunction(account.getCurrentUser)).toBe(true);
        expect(angular.isFunction(account.getActiveUsers)).toBe(true);
        expect(angular.isFunction(account.getRegisteredUsers)).toBe(true);
        expect(angular.isFunction(account.updateCurrentUser)).toBe(true);
        expect(angular.isFunction(account.updateUser)).toBe(true);
        expect(angular.isFunction(account.activate)).toBe(true);
        expect(angular.isFunction(account.sendActivationEmail)).toBe(true);
        expect(angular.isFunction(account.resetPassword)).toBe(true);
        expect(angular.isFunction(account.sendPasswordResetEmail)).toBe(true);
        expect(angular.isFunction(account.getReports)).toBe(true);
    });

    describe("calls the appropriate server api", function() {
        var dummyResult = "dummyResult";
    	
    	it('to register a user', function() {
    		var result, promise = account.register("string1", "string2", "string3", "string4", "string5");

            $httpBackend.expectPOST('/api/register', '{"apiVersion":"1.0","body":{"name":"string1","role":"string2","email":"string3","password":"string4","managerEmail":"string5"}}').respond(200, dummyResult);
    		
    		// set the response value
    		promise.then(function(data) {
    			result = data.data;
    		});
    		expect(result).toBeUndefined(); // it really should at this point
    		$httpBackend.flush();

    		expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
    	});

    	it('to update the current user', function() {
    		var result, promise = account.updateCurrentUser("string1", "string2", "string3");

            $httpBackend.expectPUT('/api/user/update', '{"apiVersion":"1.0","body":{"name":"string1","role":"string2","managerEmail":"string3"}}').respond(200, dummyResult);

    		// set the response value
    		promise.then(function(data) {
    			result = data.data;
    		});
    		expect(result).toBeUndefined();
    		$httpBackend.flush();

    		expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
    	});

    	it('to update the current user', function() {
    		var result, promise = account.updateUser("email","string1", "string2", "string3", "string4", "string5");

            $httpBackend.expectPUT('/api/user/update/email', '{"apiVersion":"1.0","body":{"name":"string1","role":"string2","managerEmail":"string3","isAdmin":"string4","isEnabled":"string5"}}').respond(200, dummyResult);

    		// set the response value
    		promise.then(function(data) {
    			result = data.data;
    		});
    		expect(result).toBeUndefined();
    		$httpBackend.flush();

    		expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
    	});

      it('to retrieve the current user', function() {
          var result, promise = account.getCurrentUser();

          $httpBackend.expectGET('/api/user').respond(200, dummyResult);

          // set the response value
          promise.then(function(data) {
              result = data.data;
          });
          expect(result).toBeUndefined(); // it really should at this point
          $httpBackend.flush();

          expect(result).toBeDefined();
          expect(result).toEqual(dummyResult);
      });

      it('to retrieve active users', function() {
          var result, promise = account.getActiveUsers();

          $httpBackend.expectGET('/api/user/active').respond(200, dummyResult);

          // set the response value
          promise.then(function(data) {
              result = data.data;
          });
          expect(result).toBeUndefined(); // it really should at this point
          $httpBackend.flush();

          expect(result).toBeDefined();
          expect(result).toEqual(dummyResult);
      });

      it('to retrieve all users', function() {
          var result, promise = account.getRegisteredUsers();

          $httpBackend.expectGET('/api/user/all').respond(200, dummyResult);

          // set the response value
          promise.then(function(data) {
              result = data.data;
          });
          expect(result).toBeUndefined(); // it really should at this point
          $httpBackend.flush();

          expect(result).toBeDefined();
          expect(result).toEqual(dummyResult);
      });

      it('to activate a user', function() {
          var result, promise = account.activate("string1", "string2");

          $httpBackend.expectPOST('/api/register/activate', '{"apiVersion":"1.0","body":{"email":"string1","token":"string2"}}').respond(200, dummyResult);

          // set the response value
          promise.then(function(data) {
              result = data.data;
          });
          expect(result).toBeUndefined(); // it really should at this point
          $httpBackend.flush();

          expect(result).toBeDefined();
          expect(result).toEqual(dummyResult);
      });

      it('to request an activation email be sent', function() {
          var result, promise = account.sendActivationEmail("thisIsEMAIL");

          $httpBackend.expectPOST('/api/activate/email', '{"apiVersion":"1.0","body":{"username":"thisIsEMAIL"}}').respond(200, dummyResult);

          // set the response value
          promise.then(function(data) {
              result = data.data;
          });
          expect(result).toBeUndefined(); // it really should at this point
          $httpBackend.flush();

          expect(result).toBeDefined();
          expect(result).toEqual(dummyResult);
      });

      it('to reset a password', function() {
          var result, promise = account.resetPassword("string1", "string2", "string3");

          $httpBackend.expectPOST('/api/password/reset', '{"apiVersion":"1.0","body":{"password":"string1","token":"string2","username":"string3"}}').respond(200, dummyResult);

          // set the response value
          promise.then(function(data) {
              result = data.data;
          });
          expect(result).toBeUndefined(); // it really should at this point
          $httpBackend.flush();

          expect(result).toBeDefined();
          expect(result).toEqual(dummyResult);
      });

      it('to request a password email be sent', function() {
          var result, promise = account.sendPasswordResetEmail("thisIsEMAIL");

          $httpBackend.expectPOST('/api/password/reset/email', '{"apiVersion":"1.0","body":{"email":"thisIsEMAIL"}}').respond(200, dummyResult);

          // set the response value
          promise.then(function(data) {
              result = data.data;
          });
          expect(result).toBeUndefined(); // it really should at this point
          $httpBackend.flush();

          expect(result).toBeDefined();
          expect(result).toEqual(dummyResult);
      });

      it('to get the list of people reporting to the current user', function() {
          var result, promise = account.getReports();

          $httpBackend.expectGET('/api/user/reports').respond(200, dummyResult);

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

    describe('validates the request parameters', function() {
        var $scope;

        beforeEach(inject(function($rootScope){
            $scope = $rootScope.$new();
        }));

        it('to register a user with no parameters', function() {
            var success, failure, promise = account.register();

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });

        it('to register a user with one parameter', function() {
            var success, failure, promise = account.register("string1");

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });

        it('to register a user with two parameters', function() {
            var success, failure, promise = account.register("string1", "string2");

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });

        it('to register a user with three parameters', function() {
            var success, failure, promise = account.register("string1", "string2","string3");

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });

        it('to activate a user with no parameters', function() {
            var success, failure, promise = account.activate();

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });

        it('to activate a user with one parameter', function() {
            var success, failure, promise = account.activate("string1");

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });

        it('to request an activation email with no parameters', function() {
            var success, failure, promise = account.sendActivationEmail();

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });

        it('to reset a password with no parameters', function() {
            var success, failure, promise = account.resetPassword();

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });

        it('to reset a password with one parameter', function() {
            var success, failure, promise = account.resetPassword("string1");

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });

        it('to reset a password with two parameters', function() {
            var success, failure, promise = account.resetPassword("string1", "string2");

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });

        it('to request a password reset email with no parameters', function() {
            var success, failure, promise = account.sendPasswordResetEmail();

            promise.then(function(result){
                success = result;
            }, function(result) {
                failure = result;
            });

            $scope.$digest();
            expect(success).toBeUndefined();
            expect(failure).toEqual("Invalid request");
        });
    });
});