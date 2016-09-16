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
        spyOn(model, 'getProfile').and.returnValue(deferred.promise);
        spyOn(model, 'updateProfile').and.returnValue(deferred.promise);

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
            expect(model.getProfile).toHaveBeenCalled();
    	});

	});

    describe('updates user details', function() {

        it('should reject matching email addresses', function() {

        });

        it('should call the update function', function() {

        });
    });
});
