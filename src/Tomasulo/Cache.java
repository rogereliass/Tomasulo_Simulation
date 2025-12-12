package Tomasulo;

import java.util.HashMap;
import java.util.Map;

public class Cache {
	private int cacheSize; // in bytes
	private int blockSize; // in bytes
	private int hitLatency; // cycles
	private int missPenalty; // cycles
	private Map<Integer, CacheBlock> cacheBlocks; // Maps block number to cache block
	private Memory mainMemory;
	
	public Cache(int cacheSize, int blockSize, int hitLatency, int missPenalty, Memory mainMemory) {
		this.cacheSize = cacheSize;
		this.blockSize = blockSize;
		this.hitLatency = hitLatency;
		this.missPenalty = missPenalty;
		this.mainMemory = mainMemory;
		this.cacheBlocks = new HashMap<>();
	}
	
	/**
	 * Get the block number for a given memory address
	 */
	private int getBlockNumber(int address) {
		return address / blockSize;
	}
	
	/**
	 * Get the offset within a block for a given memory address
	 */
	private int getBlockOffset(int address) {
		return address % blockSize;
	}
	
	/**
	 * Load data from cache/memory. Returns the latency in cycles.
	 * For word (4 bytes), loads from address, address+1, address+2, address+3
	 * For double (8 bytes), loads from address to address+7
	 */
	public CacheResult load(int address, int dataSize) {
		int blockNum = getBlockNumber(address);
		int offset = getBlockOffset(address);
		
		// Check if block is in cache
		if (cacheBlocks.containsKey(blockNum)) {
			// Cache hit
			CacheBlock block = cacheBlocks.get(blockNum);
			double[] data = new double[dataSize];
			for (int i = 0; i < dataSize; i++) {
				int byteAddr = address + i;
				int blockOffset = getBlockOffset(byteAddr);
				if (blockOffset < block.data.length) {
					data[i] = block.data[blockOffset];
				} else {
					// Crosses block boundary - need to load from next block
					// For simplicity, we'll handle this as a miss
					return loadFromMemory(address, dataSize, blockNum);
				}

				
			}
			return new CacheResult(data, hitLatency, true);
		} else {
			// Cache miss - load block from memory
			return loadFromMemory(address, dataSize, blockNum);
		}
	}
	
	/**
	 * Probe load latency without modifying cache state (hit or miss latency).
	 */
	public int getLoadLatency(int address, int dataSize) {
		int blockNum = getBlockNumber(address);
		int offset = getBlockOffset(address);
		// If block already present, it's a hit. If not, miss penalty applies.
		if (cacheBlocks.containsKey(blockNum)) {
			return hitLatency;
		}
		return hitLatency + missPenalty;
	}
	
	private CacheResult loadFromMemory(int address, int dataSize, int blockNum) {
		// Load the entire block from memory
		CacheBlock block = new CacheBlock(blockNum, blockSize);
		int blockStartAddr = blockNum * blockSize;
		for (int i = 0; i < blockSize && (blockStartAddr + i) < mainMemory.getArr().length; i++) {
			block.data[i] = mainMemory.getArr()[blockStartAddr + i];
		}
		
		// Evict if cache is full (simple FIFO - remove oldest if needed)
		if (cacheBlocks.size() >= (cacheSize / blockSize)) {
			// Remove first block (simple eviction)
			if (!cacheBlocks.isEmpty()) {
				cacheBlocks.remove(cacheBlocks.keySet().iterator().next());
			}
		}
		
		cacheBlocks.put(blockNum, block);
		
		// Extract requested data
		double[] data = new double[dataSize];
		for (int i = 0; i < dataSize; i++) {
			int byteAddr = address + i;
			int blockOffset = getBlockOffset(byteAddr);
			if (blockOffset < block.data.length) {
				data[i] = block.data[blockOffset];
			}
		}
		
		return new CacheResult(data, hitLatency + missPenalty, false);
	}
	
	/**
	 * Store data to cache/memory. Returns the latency in cycles.
	 */
	public CacheResult store(int address, double[] data) {
		int blockNum = getBlockNumber(address);
		int offset = getBlockOffset(address);
		
		// Write to main memory
		for (int i = 0; i < data.length && (address + i) < mainMemory.getArr().length; i++) {
			mainMemory.getArr()[address + i] = data[i];
			
		}
		System.out.println("MAIN MEMORY PRINT" + mainMemory.toString());
		System.out.println(mainMemory.getArr()[0]);
		System.out.println(mainMemory.getArr()[8]);
	
		// Update cache if block is present
		if (cacheBlocks.containsKey(blockNum)) {
			CacheBlock block = cacheBlocks.get(blockNum);
			for (int i = 0; i < data.length; i++) {
				int byteAddr = address + i;
				int blockOffset = getBlockOffset(byteAddr);
				if (blockOffset < block.data.length) {
					block.data[blockOffset] = data[i];
				}
			}
			return new CacheResult(null, hitLatency, true);
		} else {
			// Write-through: data written to memory, cache not updated on miss
			return new CacheResult(null, hitLatency + missPenalty, false);
		}
	}

	/**
	 * Probe store latency without performing the store (hit or miss latency).
	 */
	public int getStoreLatency(int address, double[] data) {
		int blockNum = getBlockNumber(address);
		if (cacheBlocks.containsKey(blockNum)) {
			return hitLatency;
		}
		return hitLatency + missPenalty;
	}
	
	/**
	 * Check if two addresses are in the same block (for address clash detection)
	 */
	public boolean sameBlock(int addr1, int addr2) {
		return getBlockNumber(addr1) == getBlockNumber(addr2);
	}
	
	/**
	 * Check if address ranges overlap (for address clash detection)
	 */
	public boolean addressesOverlap(int addr1, int size1, int addr2, int size2) {
		int end1 = addr1 + size1 - 1;
		int end2 = addr2 + size2 - 1;
		return !(end1 < addr2 || end2 < addr1);
	}
	
	public int getHitLatency() {
		return hitLatency;
	}
	
	public int getMissPenalty() {
		return missPenalty;
	}
	
	public String getCacheStatus() {
		return "Cache: " + cacheBlocks.size() + " blocks loaded, Size: " + cacheSize + " bytes, Block Size: " + blockSize + " bytes";
	}
	
	private static class CacheBlock {
		int blockNumber;
		double[] data;
		
		CacheBlock(int blockNumber, int blockSize) {
			this.blockNumber = blockNumber;
			this.data = new double[blockSize];
		}
	}
	
	public static class CacheResult {
		public double[] data;
		public int latency;
		public boolean hit;
		
		public CacheResult(double[] data, int latency, boolean hit) {
			this.data = data;
			this.latency = latency;
			this.hit = hit;
		}
	}
}

