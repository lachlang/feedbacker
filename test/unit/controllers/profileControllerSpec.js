'use strict';

describe('edit feedback detail controller [ProfileCtrl]', function() {

	var scope, profileController, model, location;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    	model = _Model_;
        spyOn(model, 'getCurrentUser').and.returnValue(deferred.promise);
        spyOn(model, 'updateCurrentUser').and.returnValue(deferred.promise);

		profileController = $controller('ProfileCtrl',{$scope: scope });
		spyOn(profileController, 'initialise').and.callThrough();
	}));

    describe('has valid intiatialisation values', function() {

    	it('should define functions', function() {
            expect(angular.isFunction(profileController.initialise)).toBe(true);
            expect(angular.isFunction(profileController.update)).toBe(true);
    	});

    	it('for global controller variables', function() {
            expect(profileController).toBeDefined();
            expect(profileController.name).not.toBeDefined();
            expect(profileController.role).not.toBeDefined();
            expect(profileController.email).not.toBeDefined();
            expect(profileController.managerEmail).not.toBeDefined();
            expect(profileController.error).not.toBeDefined();
            expect(profileController.message).not.toBeDefined();
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getCurrentUser).toHaveBeenCalled();
    	});

	});

    describe('updates user details', function() {

        it('should reject matching email addresses', function() {
            profileController.email = "test"
            profileController.managerEmail = "test"
            profileController.update()

            expect(profileController.error).toEqual("You cannot set your manager email to your be own email.");
            expect(profileController.message).toBeUndefined();
            expect(model.updateCurrentUser).not.toHaveBeenCalled();
        });

        it('should call the update function', function() {
            profileController.name = "test 1"
            profileController.role = "test 2"
            profileController.email = "test 3"
            profileController.managerEmail = "test 4"

            profileController.update();

            deferred.resolve(
                {
                    "name": "result 1",
                    "role": "result 2",
                    "credentials": {"email": "result 3"},
                    "managerEmail": "result 4"
                }
            );
            scope.$digest();

            expect(model.updateCurrentUser).toHaveBeenCalledWith("test 1", "test 2", "test 3", "test 4");
            expect(profileController.error).toBeUndefined();
            expect(profileController.message).toEqual("Your profile details have been successfully updated.");
            expect(profileController.name).toEqual("result 1");
            expect(profileController.email).toEqual("result 3");
            expect(profileController.role).toEqual("result 2");
            expect(profileController.managerEmail).toEqual("result 4");
        });

        it('should set error when update fails', function() {
            profileController.name = "test 1"
            profileController.role = "test 2"
            profileController.email = "test 3"
            profileController.managerEmail = "test 4"

            profileController.update();

            deferred.reject();
            scope.$digest();

            expect(model.updateCurrentUser).toHaveBeenCalled();
            expect(profileController.error).toEqual("We could not update your profile at this time.");
            expect(profileController.message).toBeUndefined();
        });

        it('should not allow null updates', function() {
            profileController.name = "test 1"
            profileController.role = "test 2"
            profileController.email = "test 3"

            profileController.update();
            expect(model.updateCurrentUser).not.toHaveBeenCalled();
            expect(profileController.error).toEqual("Cannot set blank values.");

            profileController.name = undefined
            profileController.managerEmail = "test 4"
            profileController.update();
            expect(model.updateCurrentUser).not.toHaveBeenCalled();
            expect(profileController.error).toEqual("Cannot set blank values.");

            profileController.name = "test 1"
            profileController.role = undefined
            profileController.update();
            expect(model.updateCurrentUser).not.toHaveBeenCalled();
            expect(profileController.error).toEqual("Cannot set blank values.");

            profileController.role = "test 2"
            profileController.email = undefined
            profileController.update();
            expect(model.updateCurrentUser).not.toHaveBeenCalled();
            expect(profileController.error).toEqual("Cannot set blank values.");
        })
    });
});
