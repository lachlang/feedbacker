'use strict';

describe('service [Feedback]', function() {
	
	var feedback, $httpBackend;

    beforeEach(module('feedbacker.services'));
    
    beforeEach(inject(function(_Feedback_, _$httpBackend_) {

    	feedback = _Feedback_;
    	$httpBackend = _$httpBackend_;
    }));
    
    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
      });
    
    it('can get an instantce of itself', function(){
    	expect(feedback).toBeDefined();
    });
    
    it('has defined functions', function() {
        expect(angular.isFunction(feedback.getPendingFeedbackActions)).toBe(true);
        expect(angular.isFunction(feedback.updateFeedback)).toBe(true);
        expect(angular.isFunction(feedback.getFeedbackItem)).toBe(true);
        expect(angular.isFunction(feedback.getCurrentFeedbackItemsForUser)).toBe(true);
        expect(angular.isFunction(feedback.getCurrentFeedbackItemsForSelf)).toBe(true);
        expect(angular.isFunction(feedback.getFeedbackHistoryForUser)).toBe(true);
        expect(angular.isFunction(feedback.getFeedbackHistoryForSelf)).toBe(true);
        expect(angular.isFunction(feedback.getActiveFeedbackCycles)).toBe(true);
        expect(angular.isFunction(feedback.getAllFeedbackCycles)).toBe(true);
        expect(angular.isFunction(feedback.getFeedbackCycle)).toBe(true);
        expect(angular.isFunction(feedback.createAdHocFeedback)).toBe(true);
        expect(angular.isFunction(feedback.getAdHocFeedbackForSelf)).toBe(true);
        expect(angular.isFunction(feedback.getAdHocFeedbackFromSelf)).toBe(true);
        expect(angular.isFunction(feedback.getAdHocFeedbackForUser)).toBe(true);
        expect(angular.isFunction(feedback.createFeedbackCycle)).toBe(true);
        expect(angular.isFunction(feedback.updateFeedbackCycle)).toBe(true);
        expect(angular.isFunction(feedback.updateFeedbackCycle360Status)).toBe(true);
    });

    describe("calls the appropriate server api", function() {
      var dummyResult = "dummyResult";
    	
    	it('to retrieve pending feedback list', function() {
    		var result, promise = feedback.getPendingFeedbackActions();

            $httpBackend.expectGET('/api/feedback/pending').respond(200, dummyResult);
    		
    		// set the response value
    		promise.then(function(data) {
    			result = data.data;
    		});
    		expect(result).toBeUndefined(); // it really should at this point
    		$httpBackend.flush();

    		expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
    	});

        it('to update a feedback item', function() {
            var result, promise = feedback.updateFeedback(9, [{stuff: "here"}, {stuff: "mo stuff"}], true, true);

            $httpBackend.expectPUT('/api/feedback/item/9', '{"apiVersion":"1.0","body":{"questions":[{"stuff":"here"},{"stuff":"mo stuff"}],"shareFeedback":true,"submit":true}}').respond(200, dummyResult);
            
            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined(); // it really should at this point
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve a specific feedback item for a user', function() {
            var result, promise = feedback.getFeedbackItem(11);

            $httpBackend.expectGET('/api/feedback/item/11').respond(200, dummyResult);
            
            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined(); // it really should at this point
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve the current feedback for a user', function() {
            var result, promise = feedback.getCurrentFeedbackItemsForUser(10);

            $httpBackend.expectGET('/api/feedback/current/10').respond(200, dummyResult);
            
            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined(); // it really should at this point
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve the current feedback for the current user', function() {
            var result, promise = feedback.getCurrentFeedbackItemsForSelf();

            $httpBackend.expectGET('/api/feedback/current/self').respond(200, dummyResult);
            
            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined(); // it really should at this point
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve feedback history for user', function() {
            var result, promise = feedback.getFeedbackHistoryForUser(11);

            $httpBackend.expectGET('/api/feedback/history/11').respond(200, dummyResult);
            
            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined(); // it really should at this point
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve feedback history for self', function() {
            var result, promise = feedback.getFeedbackHistoryForSelf();

            $httpBackend.expectGET('/api/feedback/history/self').respond(200, dummyResult);
            
            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined(); // it really should at this point
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve the active feedback cycles', function() {
            var result, promise = feedback.getActiveFeedbackCycles();

            $httpBackend.expectGET('/api/cycle/active').respond(200, dummyResult);

            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined(); // it really should at this point
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve all feedback cycles', function() {
            var result, promise = feedback.getAllFeedbackCycles();

            $httpBackend.expectGET('/api/cycle').respond(200, dummyResult);

            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined(); // it really should at this point
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve a feedback cycle by id', function() {
            var result, promise = feedback.getFeedbackCycle(123);

            $httpBackend.expectGET('/api/cycle/123').respond(200, dummyResult);

            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined();
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to create a piece of ad-hoc feedback', function() {
            var result, promise = feedback.createAdHocFeedback("toUsername", "feedback message", true);

            $httpBackend.expectPOST('/api/feedback/adhoc', '{"apiVersion":"1.0","body":{"recipientEmail":"toUsername","message":"feedback message","publishToRecipient":true}}').respond(200, dummyResult);

            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined();
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve ad-hoc feedback for any user', function() {
            var result, promise = feedback.getAdHocFeedbackForUser("username");

            $httpBackend.expectGET('/api/feedback/adhoc/username').respond(200, dummyResult);

            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined();
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve ad-hoc feedback for a the current user', function() {
            var result, promise = feedback.getAdHocFeedbackForSelf();

            $httpBackend.expectGET('/api/feedback/adhoc/self').respond(200, dummyResult);

            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined();
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to retrieve ad-hoc feedback from a user', function() {
            var result, promise = feedback.getAdHocFeedbackFromSelf();

            $httpBackend.expectGET('/api/feedback/adhoc/from').respond(200, dummyResult);

            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined();
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to create a new feedback cycle', function() {
            var result, promise = feedback.createFeedbackCycle({"some":"thing"});

            $httpBackend.expectPOST('/api/cycle', '{"apiVersion":"1.0","body":{"some":"thing"}}').respond(200, dummyResult);

            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined();
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to update an existing feedback cycle details', function() {
            var result, promise = feedback.updateFeedbackCycle({"id":123, "some":"thing"});

            $httpBackend.expectPUT('/api/cycle/123', '{"apiVersion":"1.0","body":{"id":123,"some":"thing"}}').respond(200, dummyResult);

            // set the response value
            promise.then(function(data) {
                result = data.data;
            });
            expect(result).toBeUndefined();
            $httpBackend.flush();

            expect(result).toBeDefined();
            expect(result).toEqual(dummyResult);
        });

        it('to update the 360 degree status for a feedback cycle', function() {
            var result, promise = feedback.updateFeedbackCycle360Status(123, true, "username");

            $httpBackend.expectPUT('/api/cycle/123/360', '{"apiVersion":"1.0","body":{"topLevelReviewUsername":"username","status":true}}').respond(200, dummyResult);

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

});
