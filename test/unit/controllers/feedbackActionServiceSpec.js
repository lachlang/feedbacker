'use strict';

describe('connection request controller [FeedbackActionCtrl]', function() {

	var scope, feedbackActionController, model;
	var deferred;

	beforeEach(module('feedbackerApp'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    	model = _Model_;
		spyOn(model, 'getPendingFeedbackActions').and.returnValue(deferred.promise);

		feedbackActionController = $controller('FeedbackActionCtrl',{$scope: scope });
	}));

    describe('has valid intiatialisation values', function() {

    	// it('should define functions', function() {
    	// 	expect(angular.isFunction(feedbackActionController.getPendingFeedbackActions)).toBe(true);
    	// });

    	it('for global controller variables', function() {
            expect(feedbackActionController).toBeDefined();
            expect(feedbackActionController.actions).toBeDefined();
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
			expect(model.getPendingFeedbackActions).toHaveBeenCalled();
    	});

	});

});
