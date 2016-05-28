'use strict';

describe('nomination controller [NominationCtrl]', function() {

	var scope, nominationController, model;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    	model = _Model_;
        spyOn(model, 'getNomineeCandidates').and.returnValue(deferred.promise);
        spyOn(model, 'getCurrentNominations').and.returnValue(deferred.promise);

		nominationController = $controller('NominationCtrl',{$scope: scope });
	}));

    describe('has valid intiatialisation values', function() {

    	it('should define functions', function() {
            expect(angular.isFunction(nominationController.addNomination)).toBe(true);
            expect(angular.isFunction(nominationController.cancelNomination)).toBe(true);
            expect(angular.isFunction(nominationController.searchForNomination)).toBe(true);
    	});

    	it('for global controller variables', function() {
            expect(nominationController).toBeDefined();
            expect(nominationController.nominations).toBeDefined();
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getCurrentNominations).toHaveBeenCalled();
            expect(model.getNomineeCandidates).toHaveBeenCalled();
    	});

	});

});
