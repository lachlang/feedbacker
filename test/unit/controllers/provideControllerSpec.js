'use strict';

describe('provide feedback controller [ProvideCtrl]', function() {

	var scope, provideController, model;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    	model = _Model_;
        spyOn(model, 'getNomineeCandidates').and.returnValue(deferred.promise);
//        spyOn(model, 'getSubmittedAdHocFeedback').and.returnValue(deferred.promise);

		provideController = $controller('ProvideCtrl',{$scope: scope });
	}));

    describe('has valid initialisation values', function() {

    	it('should define functions', function() {
            expect(angular.isFunction(provideController.submitAdHocFeedback)).toBe(true);
    	});

    	it('for global controller variables', function() {
            expect(provideController).toBeDefined();
            expect(provideController.candidateList).toEqual([]);
            expect(provideController.feedbackCandidate).toBeUndefined();
            expect(provideController.publishToCandidate).toBe(false);
            expect(provideController.message).toBeUndefined();
            expect(provideController.error).toBeUndefined();
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getNomineeCandidates).toHaveBeenCalled();
//            expect(model.getCurrentNominations).toHaveBeenCalled();
    	});

	});

	describe('when submitting feedback', function() {

//		it('should call nomination.addNomination', function() {
//			nominationController.addNomination("a@b.co", 1, "personal message");
//			expect(nomination.addNomination).toHaveBeenCalledWith("a@b.co", 1, "personal message");
//		});
//
//		it('should call clear error messages', function() {
//			nominationController.error = "some value";
//
//			nominationController.addNomination("a@b.co", 1);
//			expect(nominationController.error).toBeUndefined();
//		});
//
//		it('should call set error messages for invalid parameters', function() {
//			nominationController.addNomination();
//			expect(nominationController.error).toEqual("Must send a nomination to a valid email address.");
//
//			nominationController.addNomination("a@b", 1);
//			expect(nominationController.error).toEqual("Must send a nomination to a valid email address.");
//		});
//
//		it('should call model.getCurrentNominations when successful', function() {
//			nominationController.addNomination("a@b.co", 1);
//
//			deferredNom.resolve();
//			scope.$digest();
//
//			expect(model.getCurrentNominations).toHaveBeenCalledWith(true);
//		});
//
//		it('should call set and error message when unsuccessful', function() {
//			nominationController.addNomination("a@b.co", 1);
//
//			deferredNom.reject();
//			scope.$digest();
//
//			expect(nominationController.error).toEqual("Could not create nomination at this time.  Please try again later.");
//		});
	});

});
