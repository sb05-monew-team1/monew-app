package com.codeit.monew.interest.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codeit.monew.interest.repository.InterestRepository;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

	@Mock
	private InterestRepository interestRepository;

	@InjectMocks
	private InterestService interestService;

	@Test
	@DisplayName("관심사 물리 삭제 성공")
	void deleteInterest_Success() {
		// given
		UUID interestId = UUID.randomUUID();
		when(interestRepository.existsById(interestId)).thenReturn(true);

		// when
		interestService.deleteInterest(interestId);

		// then
		verify(interestRepository, times(1)).existsById(interestId);
		verify(interestRepository, times(1)).deleteById(interestId);
	}

	@Test
	@DisplayName("없는 관심사 ID 삭제 시 예외 발생")
	void deleteInterest_Failure_NotFound() {
		// given
		UUID nonExistentId = UUID.randomUUID();
		when(interestRepository.existsById(nonExistentId)).thenReturn(false);

		// when & then
		assertThrows(NoSuchElementException.class, () -> {
			interestService.deleteInterest(nonExistentId);
		});
		verify(interestRepository, never()).deleteById(nonExistentId);
	}
}