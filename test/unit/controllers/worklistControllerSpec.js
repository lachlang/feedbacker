'use strict';

describe('worklist controller [WorklistCtrl]', function() {

	var scope, worklistController, model;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    	model = _Model_;
        spyOn(model, 'getCurrentUser').and.returnValue(deferred.promise);
        spyOn(model, 'getPendingFeedbackActions').and.returnValue(deferred.promise);

		worklistController = $controller('WorklistCtrl',{$scope: scope });
	}));

    describe('has valid initialisation values', function() {

    	it('should define functions', function() {
            // TODO: add functions here if they get added to the controller
    	});

    	it('for global controller variables', function() {
            expect(worklistController).toBeDefined();
            expect(worklistController.pendingActions).toBeDefined();
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getCurrentUser).toHaveBeenCalled();
            expect(model.getPendingFeedbackActions).toHaveBeenCalled();
    	});

	});

});
